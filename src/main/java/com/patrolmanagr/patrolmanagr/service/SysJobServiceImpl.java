package com.patrolmanagr.patrolmanagr.service;
import com.patrolmanagr.patrolmanagr.config.ScheduleTypeJob;
import com.patrolmanagr.patrolmanagr.config.JobScope;
import com.patrolmanagr.patrolmanagr.dto.SysJobDTO;
import com.patrolmanagr.patrolmanagr.entity.SysJob;
import com.patrolmanagr.patrolmanagr.exception.ApiRequestException;
import com.patrolmanagr.patrolmanagr.repository.SysJobRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SysJobServiceImpl implements SysJobService {

    @Autowired
    private SysJobRepository sysJobRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public SysJob createJob(SysJobDTO jobDTO) {
        // Vérifier l'unicité du code
        Optional<SysJob> existingJob = sysJobRepository.findByJobCode(jobDTO.getJobCode());
        if (existingJob.isPresent()) {
            throw new ApiRequestException("Un job avec ce code existe déjà");
        }

        // Valider les paramètres selon le scheduleTypeJob
        validateJobParameters(jobDTO);

        // Mapper le DTO vers l'entité
        SysJob job = modelMapper.map(jobDTO, SysJob.class);

        // Convertir la liste d'IDs en string
        if (jobDTO.getRondeIds() != null) {
            job.setRondeIds(convertRondeIdsToString(jobDTO.getRondeIds()));
        }

        return sysJobRepository.save(job);
    }

    @Override
    public SysJob updateJob(Long id, SysJobDTO jobDTO) {
        SysJob existingJob = sysJobRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));

        // Vérifier l'unicité du code (sauf pour le job actuel)
        if (!existingJob.getJobCode().equals(jobDTO.getJobCode())) {
            Optional<SysJob> jobWithSameCode = sysJobRepository.findByJobCode(jobDTO.getJobCode());
            if (jobWithSameCode.isPresent() && !jobWithSameCode.get().getId().equals(id)) {
                throw new ApiRequestException("Un autre job avec ce code existe déjà");
            }
        }

        // Valider les paramètres
        validateJobParameters(jobDTO);

        // Mettre à jour les champs
        existingJob.setJobCode(jobDTO.getJobCode());
        existingJob.setName(jobDTO.getName());
        existingJob.setDescription(jobDTO.getDescription());
        existingJob.setScheduleTypeJob(jobDTO.getScheduleTypeJob());
        existingJob.setScheduleTime(jobDTO.getScheduleTime());
        existingJob.setScheduleIntervalMin(jobDTO.getScheduleIntervalMin());
        existingJob.setJobScope(jobDTO.getJobScope());
        existingJob.setParamsJson(jobDTO.getParamsJson());
        existingJob.setIsEnabled(jobDTO.getIsEnabled());
        existingJob.setAuditField(jobDTO.getAuditField());

        if (jobDTO.getRondeIds() != null) {
            existingJob.setRondeIds(convertRondeIdsToString(jobDTO.getRondeIds()));
        }

        return sysJobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(Long id) {
        SysJob job = sysJobRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));
        sysJobRepository.delete(job);
    }

    @Override
    public SysJob getJobById(Long id) {
        return sysJobRepository.findById(id)
                .orElseThrow(() -> new ApiRequestException("Job non trouvé"));
    }

    @Override
    public SysJob getJobByCode(String jobCode) {
        return sysJobRepository.findByJobCode(jobCode)
                .orElseThrow(() -> new ApiRequestException("Job avec code " + jobCode + " non trouvé"));
    }

    @Override
    public List<SysJob> getAllJobs() {
        return sysJobRepository.findAll();
    }

    @Override
    public List<SysJob> getActiveJobs() {
        return sysJobRepository.findByIsEnabledTrue();
    }

    @Override
    public List<SysJob> getJobsByScheduleType(String scheduleType) {
        try {
            ScheduleTypeJob scheduleTypeJob = ScheduleTypeJob.valueOf(scheduleType.toUpperCase());
            return sysJobRepository.findByScheduleTypeJob(scheduleTypeJob);
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Type de schedule invalide: " + scheduleType);
        }
    }

    @Override
    public List<SysJob> getJobsByScope(String scope) {
        try {
            JobScope jobScope = JobScope.valueOf(scope.toUpperCase());
            return sysJobRepository.findByJobScope(jobScope);
        } catch (IllegalArgumentException e) {
            throw new ApiRequestException("Scope invalide: " + scope);
        }
    }

    @Override
    public void toggleJobStatus(Long id, Boolean isEnabled) {
        SysJob job = getJobById(id);
        job.setIsEnabled(isEnabled);
        sysJobRepository.save(job);
    }

    private void validateJobParameters(SysJobDTO jobDTO) {
        if (jobDTO.getScheduleTypeJob() == null) {
            throw new ApiRequestException("Le type de schedule est obligatoire");
        }

        switch (jobDTO.getScheduleTypeJob()) {
            case HOURLY:
                if (jobDTO.getScheduleIntervalMin() != null && jobDTO.getScheduleIntervalMin() <= 0) {
                    throw new ApiRequestException("L'intervalle pour un job HOURLY doit être supérieur à 0");
                }
                break;

            case DAILY:
                if (jobDTO.getScheduleTime() == null) {
                    throw new ApiRequestException("L'heure de programmation est obligatoire pour un job DAILY");
                }
                break;

            case WEEKLY:
                // Validation spécifique pour WEEKLY si nécessaire
                break;

            case MANUEL:
                // Aucune validation spécifique
                break;
        }
    }

    private String convertRondeIdsToString(List<Long> rondeIds) {
        if (rondeIds == null || rondeIds.isEmpty()) {
            return null;
        }
        return String.join(",", rondeIds.stream()
                .map(String::valueOf)
                .toList());
    }
}