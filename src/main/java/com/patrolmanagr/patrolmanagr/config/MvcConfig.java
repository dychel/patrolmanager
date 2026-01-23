package com.patrolmanagr.patrolmanagr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Assurez-vous que le chemin correspond à celui configuré dans application.properties
        String userHomeDir = System.getProperty("user.home");
        String imgDirectory = "file:" + userHomeDir + "/ERP/product/images/";

        registry.addResourceHandler("/images/**").addResourceLocations(imgDirectory);
    }
}
