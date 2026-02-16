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
        Map<Integer, String> erreursLignes = new LinkedHashMap<>();
        Map<String, Set<LocalDateTime>> doublonsMap = new HashMap<>();
        Set<String> pastillesNonTrouvees = new HashSet<>();
        int compteurDoublons = 0;

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
            log.info("{} pastilles chargées sur {} identifiants", pastilleMap.size(), allPastilleUids.size());

            // Traiter chaque ligne
            int lineNumber = 0;
            for (Map<String, String> row : rawData) {
                lineNumber++;
                try {
                    FactPointageDTO dto = convertRowToPointage(row, lineNumber, pastilleMap);

                    if (dto != null) {
                        // Vérifier les doublons (même pastille dans un intervalle de 30 secondes)
                        String pastilleKey = dto.getPastilleCodeRaw();
                        LocalDateTime eventTime = dto.getEventTime();

                        boolean estDoublon = false;

                        if (doublonsMap.containsKey(pastilleKey)) {
                            for (LocalDateTime existingTime : doublonsMap.get(pastilleKey)) {
                                long diffSeconds = Math.abs(existingTime.until(eventTime, java.time.temporal.ChronoUnit.SECONDS));
                                if (diffSeconds < 30) { // Tolérance de 30 secondes
                                    estDoublon = true;
                                    compteurDoublons++;
                                    log.debug("Doublon détecté ligne {}: {} à {} (écart: {}s)",
                                            lineNumber, pastilleKey, eventTime, diffSeconds);
                                    break;
                                }
                            }
                        }

                        if (!estDoublon) {
                            pointagesDTO.add(dto);
                            result.incrementAccepted();

                            // Ajouter à la map des pointages existants
                            doublonsMap.computeIfAbsent(pastilleKey, k -> new HashSet<>()).add(eventTime);
                        }
                    }
                } catch (Exception e) {
                    String erreur = String.format("Ligne %d: %s", lineNumber, e.getMessage());
                    erreursLignes.put(lineNumber, e.getMessage());
                    result.addErreur(erreur);
                    result.incrementRejected();
                    log.warn(erreur);

                    // Compter les pastilles non trouvées
                    if (e.getMessage().contains("Pastille non trouvée")) {
                        String pastilleValue = extractPastilleValue(row);
                        if (pastilleValue != null) {
                            pastillesNonTrouvees.add(pastilleValue);
                        }
                    }
                }
            }

            // Sauvegarder les pointages (sans les doublons)
            if (!pointagesDTO.isEmpty()) {
                try {
                    List<Fact_pointage> savedPointages = factPointageService.savePointageBatch(pointagesDTO);
                    log.info("{} pointages sauvegardés en base", savedPointages.size());
                    result.setAcceptedRows(savedPointages.size());
                    result.setDuplicateRows(compteurDoublons);
                } catch (Exception e) {
                    log.error("Erreur lors de la sauvegarde batch: {}", e.getMessage());
                    result.addErreur("Erreur sauvegarde base: " + e.getMessage());
                }
            } else {
                result.setAcceptedRows(0);
                result.setDuplicateRows(compteurDoublons);
            }

            // Mettre à jour le batch avec toutes les informations
            batch.setEnded_at(LocalDateTime.now());
            batch.setTotal_rows(result.getTotalRows());
            batch.setAccepted_rows(result.getAcceptedRows());
            batch.setRejected_rows(result.getRejectedRows());
            batch.setDuplicate_rows(compteurDoublons);

            // Construire le message d'erreur détaillé
            StringBuilder errorDetails = new StringBuilder();
            if (!pastillesNonTrouvees.isEmpty()) {
                errorDetails.append("Pastilles non trouvées (").append(pastillesNonTrouvees.size()).append("): ");
                errorDetails.append(String.join(", ", pastillesNonTrouvees)).append(". ");
            }
            if (compteurDoublons > 0) {
                errorDetails.append("Doublons ignorés: ").append(compteurDoublons).append(". ");
            }
            if (!erreursLignes.isEmpty()) {
                List<String> premieresErreurs = erreursLignes.values().stream()
                        .limit(3)
                        .collect(Collectors.toList());
                errorDetails.append("Erreurs: ").append(String.join("; ", premieresErreurs));
                if (erreursLignes.size() > 3) {
                    errorDetails.append("...");
                }
            }

            // Déterminer le statut final
            if (result.getTotalRows() == 0) {
                batch.setStatus(StatusIngestBatch.FAILED);
                batch.setError_message("Fichier vide");
            } else if (result.getAcceptedRows() == 0 && result.getRejectedRows() > 0) {
                batch.setStatus(StatusIngestBatch.FAILED);
                batch.setError_message("Échec total: toutes les lignes sont rejetées");
            } else if (result.getRejectedRows() > 0 || compteurDoublons > 0 || !pastillesNonTrouvees.isEmpty()) {
                batch.setStatus(StatusIngestBatch.DONE_WITH_ERRORS);
                batch.setError_message(errorDetails.toString());
            } else {
                batch.setStatus(StatusIngestBatch.DONE);
            }

            batch.setAudit_field(String.format("Importé depuis %s - %d lignes traitées, %d acceptées, %d rejetées, %d doublons, %d pastilles non trouvées",
                    filename, rawData.size(), result.getAcceptedRows(), result.getRejectedRows(), compteurDoublons, pastillesNonTrouvees.size()));

            batch.setUpdated_by(userService.getConnectedUserId());
            ingestBatchRepository.save(batch);

            result.setBatchId(batch.getId());
            result.setSuccess(result.getAcceptedRows() > 0);
            result.setMessage(String.format("Import terminé. Acceptés: %d, Rejetés: %d, Doublons: %d, Pastilles non trouvées: %d",
                    result.getAcceptedRows(), result.getRejectedRows(), compteurDoublons, pastillesNonTrouvees.size()));

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
                    pastilleMap.put(pastille.getExternal_uid().toLowerCase(), pastille);
                    pastilleMap.put(pastille.getExternal_uid().toUpperCase(), pastille);
                }
            }

            // Chercher par code pour ceux non trouvés
            List<String> notFound = new ArrayList<>();
            for (String id : identifiers) {
                String idLower = id.toLowerCase();
                String idUpper = id.toUpperCase();
                if (!pastilleMap.containsKey(id) && !pastilleMap.containsKey(idLower) && !pastilleMap.containsKey(idUpper)) {
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
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();

                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        LocalDateTime dateTime = cell.getLocalDateTimeCellValue();
                        return dateTime.toLocalDate().toString() + " " + dateTime.toLocalTime().toString();
                    }
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);

                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());

                case FORMULA:
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case NUMERIC:
                                double formulaValue = cell.getNumericCellValue();
                                if (formulaValue == Math.floor(formulaValue)) {
                                    return String.valueOf((long) formulaValue);
                                }
                                return String.valueOf(formulaValue);
                            case STRING:
                                return cell.getStringCellValue();
                            case BOOLEAN:
                                return String.valueOf(cell.getBooleanCellValue());
                            default:
                                return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }

                case BLANK:
                    return "";

                case ERROR:
                    return "ERROR: " + cell.getErrorCellValue();

                default:
                    return "";
            }
        } catch (Exception e) {
            log.warn("Erreur lors de la lecture d'une cellule Excel: {}", e.getMessage());
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
            dateStr = dateStr.trim();
            heureStr = heureStr.trim();

            log.debug("Parsing date: '{}', heure: '{}'", dateStr, heureStr);

            if (heureStr.contains("T") && heureStr.contains("-")) {
                try {
                    return LocalDateTime.parse(heureStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    // Ignorer
                }
            }

            if (heureStr.contains("1899-12-31")) {
                log.debug("Détection du format Excel 1899-12-31");
                if (heureStr.contains("T")) {
                    String[] parts = heureStr.split("T");
                    heureStr = parts[parts.length - 1];
                } else {
                    heureStr = heureStr.replace("1899-12-31", "").trim();
                }
                log.debug("Heure extraite: '{}'", heureStr);
            }

            if (heureStr.contains(".")) {
                heureStr = heureStr.substring(0, heureStr.indexOf("."));
            }

            String dateTimeStr = dateStr + " " + heureStr;
            log.debug("Tentative avec: '{}'", dateTimeStr);

            List<DateTimeFormatter> formatters = Arrays.asList(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            );

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDateTime result = LocalDateTime.parse(dateTimeStr, formatter);
                    log.debug("Parsing réussi avec format: {}", formatter.toString());
                    return result;
                } catch (DateTimeParseException e) {
                    // Essayer suivant
                }
            }

            LocalDate date = parseDate(dateStr);
            LocalTime time = parseTime(heureStr);

            if (date != null && time != null) {
                log.debug("Parsing séparé réussi: date={}, time={}", date, time);
                return LocalDateTime.of(date, time);
            }

            throw new Exception("Format de date/heure non reconnu: " + dateStr + " " + heureStr);

        } catch (Exception e) {
            log.error("Erreur parsing date/heure: {} - {}", e.getMessage(), dateStr + " " + heureStr);
            throw new Exception("Erreur parsing date/heure: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;

        dateStr = dateStr.trim();

        List<String> datePatterns = Arrays.asList(
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "dd-MM-yyyy",
                "yyyyMMdd",
                "ddMMyyyy"
        );

        for (String pattern : datePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Essayer suivant
            }
        }

        log.warn("Impossible de parser la date: {}", dateStr);
        return null;
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;

        timeStr = timeStr.trim();

        if (timeStr.contains("T")) {
            String[] parts = timeStr.split("T");
            timeStr = parts[parts.length - 1];
        }

        List<String> timePatterns = Arrays.asList(
                "HH:mm:ss",
                "HH:mm",
                "HHmmss",
                "HHmm",
                "H:mm:ss",
                "H:mm"
        );

        for (String pattern : timePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalTime.parse(timeStr, formatter);
            } catch (DateTimeParseException e) {
                // Essayer suivant
            }
        }

        log.warn("Impossible de parser le temps: {}", timeStr);
        return null;
    }

    private Ref_site findSiteByCodeOrName(String siteValue) {
        try {
            try {
                Long siteId = Long.parseLong(siteValue);
                return refSiteRepository.findByIdSite(siteId);
            } catch (NumberFormatException e) {
                // Ignorer
            }

            try {
                Ref_site site = refSiteRepository.findByCode(siteValue);
                if (site != null) return site;
            } catch (Exception e) {
                // Ignorer
            }

            return refSiteRepository.findByName(siteValue);

        } catch (Exception e) {
            log.debug("Site non trouvé: {}", siteValue);
            return null;
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
            dto.setEventDate(eventTime.toLocalDate()); // Correction: LocalDate au lieu de String
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

        // ========== 4. CHERCHER LA PASTILLE DANS LA MAP (AVEC GESTION DE CASSE) ==========
        Ref_pastille pastille = null;
        if (pastilleMap.containsKey(pastilleValue)) {
            pastille = pastilleMap.get(pastilleValue);
        } else if (pastilleMap.containsKey(pastilleValue.toLowerCase())) {
            pastille = pastilleMap.get(pastilleValue.toLowerCase());
        } else if (pastilleMap.containsKey(pastilleValue.toUpperCase())) {
            pastille = pastilleMap.get(pastilleValue.toUpperCase());
        }

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

            if (!siteTrouve) {
                throw new Exception("Pastille non trouvée: " + pastilleValue);
            }
        }

        // ========== 5. VÉRIFICATION FINALE : SITE OBLIGATOIRE ==========
        if (!siteTrouve && dto.getSiteId() == null) {
            throw new Exception("Impossible de déterminer le site pour la ligne " + lineNumber +
                    " (pastille: " + pastilleValue + ", site fichier: " + siteValue + ")");
        }

        return dto;
    }
}