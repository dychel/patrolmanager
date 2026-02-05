package com.patrolmanagr.patrolmanagr.config;
import com.patrolmanagr.patrolmanagr.dto.FactPointageDTO;
import com.patrolmanagr.patrolmanagr.entity.Fact_pointage;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Configuration globale
//        modelMapper.getConfiguration()
//                .setMatchingStrategy(MatchingStrategies.STRICT)
//                .setSkipNullEnabled(true)
//                .setFieldMatchingEnabled(true)
//                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
//                .setAmbiguityIgnored(true);
//
//        // CONVERTISSEUR pour LocalDateTime -> LocalDate
//        modelMapper.addConverter(new AbstractConverter<LocalDateTime, LocalDate>() {
//            @Override
//            protected LocalDate convert(LocalDateTime source) {
//                return source != null ? source.toLocalDate() : null;
//            }
//        });
//
//        // Configuration FactPointageDTO -> Fact_pointage
//        modelMapper.createTypeMap(FactPointageDTO.class, Fact_pointage.class)
//                .addMappings(mapper -> {
//                    // 1. SAUTER l'ID
//                    mapper.skip(Fact_pointage::setId);
//
//                    // 2. Mapper eventDate via le convertisseur
//                    mapper.using(ctx -> {
//                        LocalDateTime eventTime = ((FactPointageDTO) ctx.getSource()).getEventTime();
//                        return eventTime != null ? eventTime.toLocalDate() : null;
//                    }).map(FactPointageDTO::getEventTime, Fact_pointage::setEventDate);
//
//                    // 3. Mapper les autres champs explicitement
//                    mapper.map(FactPointageDTO::getEventTime, Fact_pointage::setEventTime);
//                    mapper.map(FactPointageDTO::getSiteId, Fact_pointage::setSiteId);
//                    mapper.map(FactPointageDTO::getPastilleId, Fact_pointage::setPastilleId);
//                    mapper.map(FactPointageDTO::getPastilleCodeRaw, Fact_pointage::setPastilleCodeRaw);
//                    mapper.map(FactPointageDTO::getSiteName, Fact_pointage::setSiteName);
//                    mapper.map(FactPointageDTO::getProcessedStatus, Fact_pointage::setProcessedStatus);
//
//                    // SAUTER eventDate du DTO (il est dérivé)
//                    mapper.skip(FactPointageDTO::getEventDate, Fact_pointage::setEventDate);
//                });

        return modelMapper;
    }
}