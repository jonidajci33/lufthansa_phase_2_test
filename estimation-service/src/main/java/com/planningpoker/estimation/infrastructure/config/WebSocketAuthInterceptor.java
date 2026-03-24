package com.planningpoker.estimation.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * STOMP channel interceptor that validates JWT tokens on CONNECT frames.
 * <p>
 * Reads the {@code Authorization} header from STOMP native headers,
 * validates the JWT using Spring Security's {@link JwtDecoder}, and
 * sets the {@code Authentication} in the STOMP header accessor.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    var authorities = extractAuthorities(jwt);
                    var authentication = new UsernamePasswordAuthenticationToken(
                            jwt.getSubject(), null, authorities);
                    accessor.setUser(authentication);
                    log.debug("WebSocket STOMP CONNECT authenticated for user={}", jwt.getSubject());
                } catch (Exception ex) {
                    log.warn("WebSocket STOMP CONNECT authentication failed: {}", ex.getMessage());
                    throw new IllegalArgumentException("Invalid JWT token on STOMP CONNECT");
                }
            } else {
                log.warn("WebSocket STOMP CONNECT missing Authorization header");
                throw new IllegalArgumentException("Missing Authorization header on STOMP CONNECT");
            }
        }

        return message;
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> roles)) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + String.valueOf(role).toUpperCase()))
                .toList();
    }
}
