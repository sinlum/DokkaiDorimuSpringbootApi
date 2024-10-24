package com.DokkaiDorimu.config;

import com.DokkaiDorimu.filter.JwtRequestFilter;
import com.DokkaiDorimu.service.CustomOAuth2UserService;
import com.DokkaiDorimu.service.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final MyUserDetailsService myUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
//    private final CustomSessionInformationExpiredStrategy sessionInformationExpiredStrategy;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter,
                          MyUserDetailsService myUserDetailsService,
                          @Lazy CustomOAuth2UserService customOAuth2UserService
                          ) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.myUserDetailsService = myUserDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;


    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/authenticate", "/api/signup", "/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/articles/search", "/api/articles/{id}/comments").permitAll()
                        .requestMatchers("/api/users", "/api/users/search").hasRole("ADMIN")
                        .requestMatchers("/api/articles/like/**", "/api/articles/{id}/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/*/comments/*").authenticated()
                        .requestMatchers("/api/messages/unread-counts").authenticated()
                        .requestMatchers("/api/users/status").permitAll()
                        .requestMatchers("/api/quiz-scores").authenticated()
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2Login -> oauth2Login
                        .userInfoEndpoint(userInfoEndpoint ->
                                userInfoEndpoint.oidcUserService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                            String token = (String) oidcUser.getAttributes().get("token");
                            response.sendRedirect("http://localhost:3000/auth/callback?token=" + token);
                        })
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}