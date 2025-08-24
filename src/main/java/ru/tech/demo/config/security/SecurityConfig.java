package ru.tech.demo.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.tech.demo.config.security.filter.JsonUsernamePasswordAuthFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService uds, PasswordEncoder enc) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(enc);

        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            AuthenticationManager authManager,
            ObjectMapper objectMapper
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/public/**",
                                "/actuator/**",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                )
                // Встроенный logout инвалидирует HttpSession, Spring Session удалит ключ из Redis
                .logout(lo -> lo
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout", "POST"))
                        .deleteCookies("SESSION", "XSRF-TOKEN")
                )
                .addFilterAt(getJsonUsernamePasswordAuthFilter(authManager, objectMapper), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

    // JSON login filter at /api/auth/login
    public JsonUsernamePasswordAuthFilter getJsonUsernamePasswordAuthFilter(
            AuthenticationManager authManager,
            ObjectMapper objectMapper
    ) {
        JsonUsernamePasswordAuthFilter loginFilter = new JsonUsernamePasswordAuthFilter(
                new AntPathRequestMatcher("/api/auth/login", "POST"),
                objectMapper
        );

        loginFilter.setAuthenticationManager(authManager);

        return loginFilter;
    }
}
