//package com.kachinpedia.Kachinpedia.controller;
//
//import com.kachinpedia.Kachinpedia.entity.User;
//import com.kachinpedia.Kachinpedia.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.stream.Collectors;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/users")
//public class UserStatusController {
//    private final UserService userService;
//    private static final Logger logger = LoggerFactory.getLogger(UserStatusController.class);
//    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();
//    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
//
//    @GetMapping(value = "/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamUserStatus(Authentication authentication) {
//        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//        String userIdentifier = getUserIdentifier(authentication);
//        logger.info("New SSE connection established for user: {}", userIdentifier);
//
//        userEmitters.put(userIdentifier, emitter);
//        onlineUsers.add(userIdentifier);
//
//        emitter.onCompletion(() -> {
//            logger.info("SSE connection completed for user: {}", userIdentifier);
//            userEmitters.remove(userIdentifier);
//            onlineUsers.remove(userIdentifier);
//            broadcastOnlineUsers();
//        });
//
//        emitter.onTimeout(() -> {
//            logger.info("SSE connection timed out for user: {}", userIdentifier);
//            userEmitters.remove(userIdentifier);
//            onlineUsers.remove(userIdentifier);
//            broadcastOnlineUsers();
//        });
//
//        sendOnlineUsers(emitter);
//        broadcastOnlineUsers();
//
//        return emitter;
//    }
//    private String getUserIdentifier(Authentication authentication) {
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof User) {
//                User user = (User) principal;
//                return user.getId().toString();
//            } else if (principal instanceof String) {
//                // This is the case when the principal is just a username
//                return userService.getUserByUsername((String) principal)
//                        .map(user -> user.getId().toString())
//                        .orElseGet(() -> "anonymous-" + UUID.randomUUID().toString());
//            }
//        }
//        return "anonymous-" + UUID.randomUUID().toString();
//    }
//
//    private void sendOnlineUsers(SseEmitter emitter) {
//        try {
//            List<Map<String, Object>> onlineUserData = onlineUsers.stream()
//                    .map(this::mapUserIdentifier)
//                    .collect(Collectors.toList());
//            logger.info("Sending online users to client: {}", onlineUserData);
//            emitter.send(SseEmitter.event()
//                    .name("onlineUsers")
//                    .data(onlineUserData));
//        } catch (IOException e) {
//            logger.error("Error sending online users", e);
//            emitter.completeWithError(e);
//        }
//    }
//
//    private void broadcastOnlineUsers() {
//        userEmitters.forEach((userId, emitter) -> {
//            try {
//                emitter.send(SseEmitter.event()
//                        .name("onlineUsers")
//                        .data(onlineUsers.stream()
//                                .map(this::mapUserIdentifier)
//                                .collect(Collectors.toList())));
//            } catch (IOException e) {
//                emitter.completeWithError(e);
//            }
//        });
//    }
//
//    private Map<String, Object> mapUserIdentifier(String identifier) {
//        boolean isAuthenticated = !identifier.startsWith("anonymous-");
//        return Map.of(
//                "userId", identifier,
//                "isAuthenticated", isAuthenticated
//        );
//    }
//}