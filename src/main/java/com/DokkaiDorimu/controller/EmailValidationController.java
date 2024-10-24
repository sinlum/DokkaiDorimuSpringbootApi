package com.DokkaiDorimu.controller;

import org.springframework.web.bind.annotation.*;
import java.net.*;
import java.util.List;
import javax.naming.directory.*;
import javax.naming.*;

@RestController
@RequestMapping("/api")
public class EmailValidationController {

    @PostMapping("/validate-email")
    public boolean validateEmail(@RequestBody EmailRequest emailRequest) {
        try {
            String domain = emailRequest.getEmail().split("@")[1];
            InitialDirContext dirContext = new InitialDirContext();
            Attributes attributes = dirContext.getAttributes("dns:/" + domain, new String[]{"MX"});
            Attribute attribute = attributes.get("MX");

            return attribute != null && attribute.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static class EmailRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
