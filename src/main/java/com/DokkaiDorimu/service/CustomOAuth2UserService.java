package com.DokkaiDorimu.service;

import com.DokkaiDorimu.repository.UserRepository;
import com.DokkaiDorimu.entity.User;
import com.DokkaiDorimu.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends OidcUserService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);


    private final UserRepository userRepository;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   MyUserDetailsService myUserDetailsService,
                                   JwtUtil jwtUtil,
                                   @Lazy UserService userService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not provided by OAuth2 provider");
        }

        String name = oidcUser.getFullName() != null ? oidcUser.getFullName() : "Unknown";
        String picture = oidcUser.getPicture();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setImgUrl(picture);
                    newUser.setPassword(passwordEncoder.encode("defaultPassword")); // Use a default password and encode it
                    newUser.setRole(User.Role.USER);
                    return userRepository.save(newUser);
                });




        UserDetails userDetails = myUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
        attributes.put("token", token);

        OidcUserInfo userInfo = new OidcUserInfo(attributes);

        return new DefaultOidcUser(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oidcUser.getIdToken(),
                userInfo
        );
    }
}