package com.DokkaiDorimu.config;

import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String authToken = accessor.getFirstNativeHeader("Authorization");
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    authToken = authToken.substring(7);
                    String username = jwtUtil.extractUsername(authToken);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtUtil.validateToken(authToken, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);

                            if (userDetails instanceof User) {
                                Long userId = ((User) userDetails).getId();
                                accessor.getSessionAttributes().put("userId", userId);
                                logger.info("User ID {} stored in session attributes", userId);
                            }
                        } else {
                            logger.warn("Invalid JWT token for user: {}", username);
                        }
                    }
                } else {
                    logger.warn("No Authorization header found in WebSocket connection request");
                }
            } catch (Exception e) {
                logger.error("Error in WebSocket authentication", e);
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            // Ensure the security context is propagated for each message
            accessor.setUser(SecurityContextHolder.getContext().getAuthentication());
        }

        return message;
    }
}