package com.patrolmanagr.patrolmanagr.config;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class QuartzConfig {

    /**
     * Configuration minimale du SchedulerFactoryBean
     * La plupart des propriétés sont dans application.properties
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // Seulement ces 2 lignes sont nécessaires
        factory.setDataSource(dataSource);
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        // Laisser Spring Boot gérer le reste via application.properties
        factory.setAutoStartup(true);
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        log.info("SchedulerFactoryBean configuré avec DataSource: {}", dataSource.getClass().getSimpleName());
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        log.info("✅ Quartz Scheduler démarré avec succès");
        log.info("Nom du scheduler: {}", scheduler.getSchedulerName());
        log.info("Instance ID: {}", scheduler.getSchedulerInstanceId());
        return scheduler;
    }
}