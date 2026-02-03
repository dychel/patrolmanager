package com.patrolmanagr.patrolmanagr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker  // CETTE ANNOTATION CRÉE AUTOMATIQUEMENT simpMessagingTemplate
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-pointages")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-pointages")
                .setAllowedOriginPatterns("*");
    }

    // NE PAS AJOUTER de bean simpMessagingTemplate ici !
    // @EnableWebSocketMessageBroker le crée automatiquement
}