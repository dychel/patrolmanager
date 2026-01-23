package com.patrolmanagr.patrolmanagr.config;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
/*
        Il est important de configurer explicitement comment ModelMapper doit mapper ProductDTO en Product car Unit, Brand and Categories sont des objets.
        1-createTypeMap(...).addMappings(...) crée une configuration de mappage personnalisée pour ProductDTO à Product:
        2-mapper.skip(Product::setId) dit à ModelMapper de ne pas mapper automatiquement une propriété à setId.
        3-mapper.map(...) définit comment les unitId, brandId, et categoriesId de ProductDTO doivent être mappés sur les propriétés correspondantes de Product.
 */
//        modelMapper.createTypeMap(ProductDTO.class, Product.class)
//                .addMappings(mapper -> {
//                    mapper.skip(Product::setId);
//                    mapper.map(ProductDTO::getUnitId, (product, o) -> product.getUnit().setId((Long) o));
//                    mapper.map(ProductDTO::getBrandId, (product, o) -> product.getBrand().setId((Long) o));
//                    mapper.map(ProductDTO::getCategoriesId, (product, o) -> product.getCategories().setId((Long) o));
//                });
        return modelMapper;
    }
}
