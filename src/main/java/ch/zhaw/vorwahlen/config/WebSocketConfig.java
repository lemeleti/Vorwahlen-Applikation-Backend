package ch.zhaw.vorwahlen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configure websocket for modules.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/module");
        registry.setApplicationDestinationPrefixes("/module-socket");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // todo: duplicated allowed origin with CORSAdvidce
        registry.addEndpoint("/stomp-ws-endpoint").setAllowedOrigins("http://localhost:8081").withSockJS();
    }
}