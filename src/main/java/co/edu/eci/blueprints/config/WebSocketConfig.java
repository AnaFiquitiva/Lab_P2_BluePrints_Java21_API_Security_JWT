package co.edu.eci.blueprints.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket + STOMP para comunicación bidireccional en tiempo real.
 * 
 * Seguridad:
 * - Restringir CORS a orígenes específicos (no "*" en producción)
 * - Validar payloads en @MessageMapping
 * - Usar JWT para autenticación (opcional pero recomendado)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-blueprints")
                // Permitir solo orígenes específicos (whitelist)
                .setAllowedOrigins(allowedOrigins.split(","))
                // Si se necesita soporte retro para navegadores antiguos:
                // .withSockJS();
        
        System.out.println("✅ WebSocket STOMP configurado");
        System.out.println("   Endpoint: /ws-blueprints");
        System.out.println("   Orígenes permitidos: " + allowedOrigins);
    }
}
