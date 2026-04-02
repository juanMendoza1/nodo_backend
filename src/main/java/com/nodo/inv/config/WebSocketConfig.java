package com.nodo.inv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para los canales a los que se va a SUSCRIBIR el Frontend (React)
        config.enableSimpleBroker("/topic");
        
        // Prefijo para los mensajes que el Frontend envíe al Backend (opcional, por si quieres chatear luego)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Esta es la URL inicial a la que React se conectará para abrir la "Llamada telefónica"
        registry.addEndpoint("/ws")
        		.setAllowedOrigins("http://localhost:5173")
                .withSockJS(); // SockJS ayuda como respaldo si el navegador no soporta WebSockets puros
    }
}