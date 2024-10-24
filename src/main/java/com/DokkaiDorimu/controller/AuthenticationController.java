package com.DokkaiDorimu.controller;


import com.DokkaiDorimu.DTO.UserDTO;
import com.DokkaiDorimu.entity.AuthenticationRequest;
import com.DokkaiDorimu.entity.AuthenticationResponse;
import com.DokkaiDorimu.service.MyUserDetailsService;
import com.DokkaiDorimu.service.UserService;
import com.DokkaiDorimu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);


    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);


            return ResponseEntity.ok(new AuthenticationResponse(jwt));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

//    @GetMapping("/user/{id}/status")
//    public ResponseEntity<?> getUserStatus(@PathVariable Long id) {
//        return userService.getUserById(id)
//                .map(user -> ResponseEntity.ok().body(Map.of("id", user.getId(), "isOnline", user.isOnline())))
//                .orElse(ResponseEntity.notFound().build());
//    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDTO userDTO) {
        try {
            userService.signup(userDTO);
            return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
//    @PostMapping("/auth/logout")
//    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
//        String email = jwtUtil.extractUsername(token.substring(7)); // Remove "Bearer " prefix
//        User user = userService.getUserByEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
//        userService.setUserOnlineStatus(user.getId(), false);
//        // Additional logout logic (e.g., invalidating the token) can be added here
//        return ResponseEntity.ok().body("Logged out successfully");
//    }


}