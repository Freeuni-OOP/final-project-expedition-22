package com.example.bookstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private final WebSocketConfig config = new WebSocketConfig();

    @Test
    void registerStompEndpoints_ShouldRegisterChatEndpoint() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration registration =
                mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/chat")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns("*")).thenReturn(registration);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/chat");
        verify(registration).setAllowedOriginPatterns("*");
        verify(registration).withSockJS();
    }

    @Test
    void configureMessageBroker_ShouldSetApplicationPrefix() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void configureMessageBroker_ShouldEnableSimpleBroker() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic");
    }
}