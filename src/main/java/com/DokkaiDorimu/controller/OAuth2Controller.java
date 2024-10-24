package com.DokkaiDorimu.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OAuth2Controller {

    @GetMapping("/api/loginSuccess")
    public ResponseEntity<?> loginSuccess(@AuthenticationPrincipal OidcUser oidcUser) {
        String token = (String) oidcUser.getAttributes().get("token");
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}
