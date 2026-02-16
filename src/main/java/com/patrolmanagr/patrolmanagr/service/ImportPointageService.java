package com.patrolmanagr.patrolmanagr.service;

import com.patrolmanagr.patrolmanagr.config.BatchType;
import com.patrolmanagr.patrolmanagr.config.Source_Type;
import com.patrolmanagr.patrolmanagr.config.StatusIngestBatch;
import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.entity.*;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.IngestBatchRepository;
import com.patrolmanagr.patrolmanagr.repository.RefSiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImportPointageService {

    @Autowired
    private IngestBatchRepository ingestBatchRepository;

    @Autowired
    private FactPointageService factPointageService;

    @Autowired
    private RefPastilleService refPastilleService;

    @Autowired
    private RefSiteRepository refSiteRepository;

    @Autowired
    private RefRondeService refRondeService;

    @Autowired
    private RefTerminalService refTerminalService;

    @Autowired
    private UserService userService;

    private static final List<String> DATE_PATTERNS = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd HH:mm:ss", "dd-MM-yyyy HH:mm:ss",
            "yyyyMMdd HHmmss", "ddMMyyyy HHmmss"
    );

    public static final List<String> PASTILLE_KEYS = Arrays.asList(
            "pastille",
            "uid",
            "external_uid",
            "externaluid",
            "code",
            "pastille_code",
            "badge_id",
            "badge",
            "pastille_uid",
            "identifiant",
            "pastille_id",
            "uuid",
            "mifare",
            "uuid_mifare",
            "nfc_id"
    );

    public static final List<String> DATE_KEYS = Arrays.asList(
            "date",
            "event_date",
            "jour",
            "date_pointage"
    );

    public static final List<String> HEURE_KEYS = Arrays.asList(
            "heure",
            "time",
            "event_time",
            "timestamp",
            "heure_pointage",
            "scanned_at"
    );

    public static final List<String> TERMINAL_KEYS = Arrays.asList(
            "terminal",
            "reader",
            "gateway",
            "terminal_code",
            "reader_id",
            "terminal_id",
            "nom_pointeuse",
            "pointeuse"
    );

    public static final List<String> AGENT_KEYS = Arrays.asList(
            "agent",
            "user",
            "employee",
            "agent_code",
            "user_id",
            "agent_id",
            "matricule"
    );

    public static final List<String> SITE_KEYS = Arrays.asList(
            "site",
            "location",
            "site_code",
            "site_name",
            "site_id",
            "site_pointeuse"
    );

    @Transactional
    public ImportResult importPointagesFromFile(MultipartFile file) {
        log.info("Début import fichier: {}, taille: {}", file.getOriginalFilename(), file.getSize());

        // Créer l'enregistrement batch
        Ingest_batch batch = new Ingest_batch();
        batch.setBatch_type(BatchType.IMPORT_EXCEL);
        batch.setStarted_at(LocalDateTime.now());
        batch.setStatus(StatusIngestBatch.RUNNING);
        batch.setCreated_by(userService.getConnectedUserId());
        batch = ingestBatchRepository.save(batch);

        ImportResult result = new ImportResult(batch.getId());
        List<FactPointageDTO> pointagesDTO = new ArrayList<>();
        Map<String, List<String>> erreursLignes = new LinkedHashMap<>();

        try {
            String filename = file.getOriginalFilename();
            List<Map<String, String>> rawData;

            if (filename.endsWith(".csv")) {
                rawData = parseCsv(file);
            } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                rawData = parseExcel(file);
            } else {
                throw new ApiRequestException("Format de fichier non supporté. Utilisez CSV ou XLSX");
            }

            result.setTotalRows(rawData.size());
            log.info("{} lignes lues dans le fichier", rawData.size());

            // Vérifier les en-têtes
            if (!rawData.isEmpty()) {
                validateHeaders(rawData.get(0).keySet());
            }

            // Préparer tous les pastilles UIDs pour chargement batch
            Set<String> allPastilleUids = new HashSet<>();
            for (Map<String, String> row : rawData) {
                String pastilleValue = extractPastilleValue(row);
                if (pastilleValue != null && !pastilleValue.trim().isEmpty()) {
                    allPastilleUids.add(pastilleValue.trim());
                }
            }

            // Charger toutes les pastilles en une seule requête
            Map<String, Ref_pastille> pastilleMap = loadPastillesByAllIdentifiers(new ArrayList<>(allPastilleUids));

            // Traiter chaque ligne
            int lineNumber = 0;
            for (Map<String, String> row : rawData) {
                lineNumber++;
                try {
                    FactPointageDTO dto = convertRowToPointage(row, lineNumber, pastilleMap);
                    if (dto != null) {
                        pointagesDTO.add(dto);
                        result.incrementAccepted();
                    }
                } catch (Exception e) {
                    String erreur = String.format("Ligne %d: %s", lineNumber, e.getMessage());
                    erreursLignes.put("Ligne " + lineNumber, Collections.singletonList(e.getMessage()));
                    result.addErreur(erreur);
                    result.incrementRejected();
                    log.warn(erreur);
                }
            }

            // Sauvegarder TOUS les pointages
            if (!pointagesDTO.isEmpty()) {
                try {
                    List<Fact_pointage> savedPointages = factPointageService.savePointageBatch(pointagesDTO);
                    log.info("{} pointages sauvegardés en base", savedPointages.size());
                    result.setAcceptedRows(savedPointages.size());
                    result.setDuplicateRows(0);
                } catch (Exception e) {
                    log.error("Erreur lors de la sauvegarde batch: {}", e.getMessage());
                    result.addErreur("Erreur sauvegarde base: " + e.getMessage());
                }
            }

            // Mettre à jour le batch
            batch.setEnded_at(LocalDateTime.now());
            batch.setTotal_rows(result.getTotalRows());
            batch.setAccepted_rows(result.getAcceptedRows());
            batch.setRejected_rows(result.getRejectedRows());
            batch.setDuplicate_rows(0);

            if (result.getTotalRows() == 0) {
                batch.setStatus(StatusIngestBatch.FAILED);
                batch.setError_message("Fichier vide");
            } else if (result.getAcceptedRows() == 0 && result.getRejectedRows() > 0) {
                batch.setStatus(StatusIngestBatch.FAILED);
                batch.setError_message("Échec total: toutes les lignes sont rejetées");
            } else if (result.getRejectedRows() > 0) {
                batch.setStatus(StatusIngestBatch.DONE_WITH_ERRORS);
                String erreurs = String.join("; ", result.getErreurs().subList(0, Math.min(3, result.getErreurs().size())));
                batch.setError_message(erreurs + (result.getErreurs().size() > 3 ? "..." : ""));
            } else {
                batch.setStatus(StatusIngestBatch.DONE);
            }

            batch.setAudit_field(String.format("Importé depuis %s - %d lignes traitées", filename, rawData.size()));
            batch.setUpdated_by(userService.getConnectedUserId());
            ingestBatchRepository.save(batch);

            result.setBatchId(batch.getId());
            result.setSuccess(true);
            result.setMessage(String.format("Import terminé. Acceptés: %d, Rejetés: %d",
                    result.getAcceptedRows(), result.getRejectedRows()));

            log.info(result.getMessage());

        } catch (Exception e) {
            log.error("Erreur lors de l'import: {}", e.getMessage(), e);

            batch.setEnded_at(LocalDateTime.now());
            batch.setStatus(StatusIngestBatch.FAILED);
            batch.setError_message(e.getMessage());
            ingestBatchRepository.save(batch);

            result.setSuccess(false);
            result.setMessage("Erreur: " + e.getMessage());
        }

        return result;
    }

    private String extractPastilleValue(Map<String, String> row) {
        for (String key : PASTILLE_KEYS) {
            String value = row.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String entryKey = entry.getKey().toLowerCase();
                String entryValue = entry.getValue();
                if ((entryKey.contains(key) || key.contains(entryKey)) &&
                        entryValue != null && !entryValue.isEmpty()) {
                    return entryValue;
                }
            }
        }
        return null;
    }

    private Map<String, Ref_pastille> loadPastillesByAllIdentifiers(List<String> identifiers) {
        Map<String, Ref_pastille> pastilleMap = new HashMap<>();

        if (identifiers.isEmpty()) {
            return pastilleMap;
        }

        try {
            // Chercher par external_uid
            List<Ref_pastille> pastillesByUid = refPastilleService.findPastillesByExternalUids(identifiers);
            for (Ref_pastille pastille : pastillesByUid) {
                if (pastille.getExternal_uid() != null) {
                    pastilleMap.put(pastille.getExternal_uid(), pastille);
                }
            }

            // Chercher par code pour ceux non trouvés
            List<String> notFound = new ArrayList<>();
            for (String id : identifiers) {
                if (!pastilleMap.containsKey(id)) {
                    notFound.add(id);
                }
            }

            if (!notFound.isEmpty()) {
                for (String code : notFound) {
                    try {
                        Ref_pastille pastille = refPastilleService.findPastilleByCode(code);
                        if (pastille != null) {
                            pastilleMap.put(code, pastille);
                            if (pastille.getExternal_uid() != null) {
                                pastilleMap.put(pastille.getExternal_uid(), pastille);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Pastille non trouvée par code: {}", code);
                    }
                }
            }

            log.info("{} pastilles chargées sur {} identifiants", pastilleMap.size(), identifiers.size());

        } catch (Exception e) {
            log.error("Erreur chargement pastilles: {}", e.getMessage());
        }

        return pastilleMap;
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ApiRequestException("Fichier CSV vide");
            }

            String[] headers = headerLine.split("[,;]");
            headers = Arrays.stream(headers).map(String::trim).map(String::toLowerCase).toArray(String[]::new);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                String[] values = line.split("[,;]", -1);
                Map<String, String> row = new HashMap<>();

                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i].trim());
                }

                row.put("_line_number", String.valueOf(lineNumber));
                data.add(row);
            }
        }

        return data;
    }

    private List<Map<String, String>> parseExcel(MultipartFile file) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new ApiRequestException("Fichier Excel vide");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().toLowerCase().trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                boolean hasData = false;

                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = getCellValueAsString(cell).trim();
                    rowData.put(headers.get(j), value);
                    if (!value.isEmpty()) hasData = true;
                }

                if (hasData) {
                    rowData.put("_line_number", String.valueOf(i + 1));
                    data.add(rowData);
                }
            }
        }

        return data;
    }

    private String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void validateHeaders(Set<String> headers) {
        List<String> headersList = new ArrayList<>(headers);

        boolean hasDate = headersList.stream().anyMatch(h ->
                DATE_KEYS.stream().anyMatch(k -> h.contains(k)));

        boolean hasHeure = headersList.stream().anyMatch(h ->
                HEURE_KEYS.stream().anyMatch(k -> h.contains(k)));

        boolean hasPastille = headersList.stream().anyMatch(h ->
                PASTILLE_KEYS.stream().anyMatch(k -> h.contains(k)));

        if (!hasDate) {
            throw new ApiRequestException(
                    "Colonne date manquante. Colonnes trouvées: " + headersList
            );
        }
        if (!hasHeure) {
            throw new ApiRequestException(
                    "Colonne heure manquante. Colonnes trouvées: " + headersList
            );
        }
        if (!hasPastille) {
            throw new ApiRequestException(
                    "Colonne pastille/uid/external_uid manquante. Colonnes trouvées: " + headersList
            );
        }
    }

    private String getValueFromRow(Map<String, String> row, List<String> possibleKeys) {
        for (String key : possibleKeys) {
            String value = row.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String entryKey = entry.getKey().toLowerCase();
                if (entryKey.contains(key) && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(String dateStr, String heureStr) throws Exception {
        try {
            String dateTimeStr = dateStr + " " + heureStr;

            for (String pattern : DATE_PATTERNS) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                    return LocalDateTime.parse(dateTimeStr, formatter);
                } catch (DateTimeParseException e) {
                    // Essayer le pattern suivant
                }
            }

            LocalDate date = parseDate(dateStr);
            LocalTime time = parseTime(heureStr);

            if (date != null && time != null) {
                return LocalDateTime.of(date, time);
            }

            throw new Exception("Format de date/heure non reconnu: " + dateStr + " " + heureStr);

        } catch (Exception e) {
            throw new Exception("Erreur parsing date/heure: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        List<String> datePatterns = Arrays.asList(
                "yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "dd-MM-yyyy",
                "yyyyMMdd", "ddMMyyyy"
        );

        for (String pattern : datePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Essayer le pattern suivant
            }
        }
        return null;
    }

    private LocalTime parseTime(String timeStr) {
        List<String> timePatterns = Arrays.asList(
                "HH:mm:ss", "HH:mm", "HHmmss", "HHmm"
        );

        for (String pattern : timePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalTime.parse(timeStr, formatter);
            } catch (DateTimeParseException e) {
                // Essayer le pattern suivant
            }
        }
        return null;
    }

    private Ref_site findSiteByCodeOrName(String siteValue) {
        try {
            // Essayer par ID
            try {
                Long siteId = Long.parseLong(siteValue);
                return refSiteRepository.findByIdSite(siteId);
            } catch (NumberFormatException e) {
                // Ce n'est pas un ID, continuer
            }

            // Essayer par code
            try {
                Ref_site site = refSiteRepository.findByCode(siteValue);
                if (site != null) return site;
            } catch (Exception e) {
                // Non trouvé par code
            }

            // Essayer par nom
            return refSiteRepository.findByName(siteValue);

        } catch (Exception e) {
            throw new ApiRequestException("Site non trouvé: " + siteValue);
        }
    }

    private Long findActiveRondeForSite(Long siteId) {
        try {
            List<Ref_ronde> rondes = refRondeService.findRondeByIdSite(siteId);

            if (rondes.isEmpty()) {
                return null;
            }

            Optional<Ref_ronde> activeRonde = rondes.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus().name().equals("ACTIVE"))
                    .findFirst();

            return activeRonde.map(Ref_ronde::getId).orElse(rondes.get(0).getId());

        } catch (Exception e) {
            log.warn("Erreur recherche ronde site {}: {}", siteId, e.getMessage());
            return null;
        }
    }

    private FactPointageDTO convertRowToPointage(Map<String, String> row, int lineNumber,
                                                 Map<String, Ref_pastille> pastilleMap) throws Exception {
        FactPointageDTO dto = new FactPointageDTO();

        // ========== 1. EXTRAIRE LA DATE ET L'HEURE (OBLIGATOIRE) ==========
        String dateStr = getValueFromRow(row, DATE_KEYS);
        String heureStr = getValueFromRow(row, HEURE_KEYS);

        if (dateStr == null || dateStr.isEmpty()) {
            throw new Exception("Date manquante à la ligne " + lineNumber);
        }
        if (heureStr == null || heureStr.isEmpty()) {
            throw new Exception("Heure manquante à la ligne " + lineNumber);
        }

        try {
            LocalDateTime eventTime = parseDateTime(dateStr, heureStr);
            dto.setEventTime(eventTime);
        } catch (Exception e) {
            throw new Exception("Format de date/heure invalide à la ligne " + lineNumber + ": " + dateStr + " " + heureStr);
        }

        // ========== 2. EXTRAIRE L'IDENTIFIANT PASTILLE (OBLIGATOIRE) ==========
        String pastilleValue = extractPastilleValue(row);
        if (pastilleValue == null || pastilleValue.isEmpty()) {
            throw new Exception("Identifiant pastille manquant à la ligne " + lineNumber);
        }
        dto.setPastilleCodeRaw(pastilleValue);

        // ========== 3. EXTRAIRE LES CHAMPS OPTIONNELS ==========
        dto.setTerminalCodeRaw(getValueFromRow(row, TERMINAL_KEYS));
        dto.setAgentCodeRaw(getValueFromRow(row, AGENT_KEYS));
        String siteValue = getValueFromRow(row, SITE_KEYS);

        dto.setSourceType(Source_Type.MANUAL);

        // ========== 4. CHERCHER LA PASTILLE DANS LA MAP ==========
        Ref_pastille pastille = pastilleMap.get(pastilleValue);

        // Variable pour savoir si on a trouvé un site
        boolean siteTrouve = false;

        if (pastille != null) {
            // ========== CAS 1: PASTILLE TROUVÉE ==========
            dto.setPastilleId(pastille.getId());
            dto.setPastilleLabel(pastille.getLabel());

            // Récupérer le site depuis la pastille
            if (pastille.getRef_site_id() != null) {
                dto.setSiteId(pastille.getRef_site_id().getId());
                dto.setSiteName(pastille.getRef_site_id().getName());
                siteTrouve = true;

                // Enrichir avec la zone si disponible
                if (pastille.getRef_site_id().getRef_zone() != null) {
                    dto.setZoneId(pastille.getRef_site_id().getRef_zone().getId());
                    dto.setZoneName(pastille.getRef_site_id().getRef_zone().getName());
                }

                // Enrichir avec le secteur si disponible
                if (pastille.getRef_secteur_id() != null) {
                    dto.setSecteurId(pastille.getRef_secteur_id().getId());
                    dto.setSecteurName(pastille.getRef_secteur_id().getName());
                }

                // Chercher une ronde active pour ce site
                try {
                    Long rondeId = findActiveRondeForSite(pastille.getRef_site_id().getId());
                    if (rondeId != null) {
                        dto.setRondeId(rondeId);
                        try {
                            Ref_ronde ronde = refRondeService.findRondeById(rondeId);
                            dto.setRondeName(ronde.getCode());
                        } catch (Exception e) {
                            log.warn("Ronde non chargeable pour le site {}", pastille.getRef_site_id().getId());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Erreur recherche ronde pour le site {}", pastille.getRef_site_id().getId());
                }
            }

            dto.setProcessedStatus("PROCESSED");

        } else {
            // ========== CAS 2: PASTILLE NON TROUVÉE ==========
            log.warn("Pastille non trouvée: {} à la ligne {}", pastilleValue, lineNumber);

            // Essayer de trouver le site à partir de la valeur fournie dans le fichier
            if (siteValue != null && !siteValue.isEmpty()) {
                try {
                    Ref_site site = findSiteByCodeOrName(siteValue);
                    if (site != null) {
                        dto.setSiteId(site.getId());
                        dto.setSiteName(site.getName());
                        siteTrouve = true;
                        log.info("Site trouvé à partir du fichier: {} pour la ligne {}", site.getName(), lineNumber);
                    }
                } catch (Exception e) {
                    log.debug("Site non trouvé avec la valeur: {}", siteValue);
                }
            }

            dto.setProcessedStatus("REJECTED");
            dto.setRejectionReason("Pastille non trouvée: " + pastilleValue);
        }

        // ========== 5. VÉRIFICATION FINALE : SITE OBLIGATOIRE ==========
        if (!siteTrouve && dto.getSiteId() == null) {
            // Si on n'a pas de site, on ne peut pas sauvegarder (colonne NOT NULL)
            throw new Exception("Impossible de déterminer le site pour la ligne " + lineNumber +
                    " (pastille: " + pastilleValue + ", site fichier: " + siteValue + ")");
        }
        return dto;
    }

//    private FactPointageDTO convertRowToPointage(Map<String, String> row, int lineNumber,
//                                                 Map<String, Ref_pastille> pastilleMap) throws Exception {
//        FactPointageDTO dto = new FactPointageDTO();
//
//        // Extraire la date et l'heure (OBLIGATOIRE)
//        String dateStr = getValueFromRow(row, DATE_KEYS);
//        String heureStr = getValueFromRow(row, HEURE_KEYS);
//
//        if (dateStr == null || dateStr.isEmpty()) {
//            throw new Exception("Date manquante à la ligne " + lineNumber);
//        }
//        if (heureStr == null || heureStr.isEmpty()) {
//            throw new Exception("Heure manquante à la ligne " + lineNumber);
//        }
//
//        LocalDateTime eventTime = parseDateTime(dateStr, heureStr);
//        dto.setEventTime(eventTime);
//
//        // Extraire l'identifiant pastille (OBLIGATOIRE)
//        String pastilleValue = extractPastilleValue(row);
//        if (pastilleValue == null || pastilleValue.isEmpty()) {
//            throw new Exception("Identifiant pastille manquant à la ligne " + lineNumber);
//        }
//        dto.setPastilleCodeRaw(pastilleValue);
//
//        // Extraire les champs optionnels
//        dto.setTerminalCodeRaw(getValueFromRow(row, TERMINAL_KEYS));
//        dto.setAgentCodeRaw(getValueFromRow(row, AGENT_KEYS));
//        String siteValue = getValueFromRow(row, SITE_KEYS);
//
//        dto.setSourceType(Source_Type.MANUAL);
//
//        // Chercher la pastille dans la map
//        Ref_pastille pastille = pastilleMap.get(pastilleValue);
//
//        if (pastille != null) {
//            // Pastille trouvée - on peut enrichir avec ses données
//            dto.setPastilleId(pastille.getId());
//            dto.setPastilleLabel(pastille.getLabel());
//
//            // Récupérer le site depuis la pastille
//            if (pastille.getRef_site_id() != null) {
//                dto.setSiteId(pastille.getRef_site_id().getId());
//                dto.setSiteName(pastille.getRef_site_id().getName());
//
//                if (pastille.getRef_site_id().getRef_zone() != null) {
//                    dto.setZoneId(pastille.getRef_site_id().getRef_zone().getId());
//                    dto.setZoneName(pastille.getRef_site_id().getRef_zone().getName());
//                }
//
//                if (pastille.getRef_secteur_id() != null) {
//                    dto.setSecteurId(pastille.getRef_secteur_id().getId());
//                    dto.setSecteurName(pastille.getRef_secteur_id().getName());
//                }
//
//                // Chercher une ronde active pour ce site
//                Long rondeId = findActiveRondeForSite(pastille.getRef_site_id().getId());
//                if (rondeId != null) {
//                    dto.setRondeId(rondeId);
//                    try {
//                        Ref_ronde ronde = refRondeService.findRondeById(rondeId);
//                        dto.setRondeName(ronde.getCode());
//                    } catch (Exception e) {
//                        log.warn("Ronde non chargeable", e);
//                    }
//                }
//            } else {
//                // Pastille trouvée mais sans site associé
//                log.warn("Pastille {} trouvée mais sans site associé à la ligne {}", pastilleValue, lineNumber);
//                // On peut quand même sauvegarder avec siteId = null si la base le permet
//                // Sinon, on rejette la ligne
//            }
//            dto.setProcessedStatus("PROCESSED");
//        } else {
//            // Pastille non trouvée - on importe quand même avec les données brutes
//            log.warn("Pastille non trouvée: {} à la ligne {}", pastilleValue, lineNumber);
//            dto.setProcessedStatus("PENDING");
//            dto.setRejectionReason("Pastille non trouvée: " + pastilleValue);
//
//            // Optionnel: essayer de trouver le site par son nom si fourni dans le fichier
//            if (siteValue != null && !siteValue.isEmpty()) {
//                try {
//                    Ref_site site = findSiteByCodeOrName(siteValue);
//                    if (site != null) {
//                        dto.setSiteId(site.getId());
//                        dto.setSiteName(site.getName());
//                    }
//                } catch (Exception e) {
//                    log.debug("Site non trouvé: {}", siteValue);
//                }
//            }
//        }
//
//        return dto;
//    }
}