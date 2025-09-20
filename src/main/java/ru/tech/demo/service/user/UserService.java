package ru.tech.demo.service.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.tech.demo.model.TenantID;
import ru.tech.demo.service.jwt.JwtTokenService;

import java.util.List;

@Service
public class UserService {
    private final WebClient userServiceWebClient;
    private final JwtTokenService jwtTokenService;

    public UserService(
            @Qualifier("userServiceWebClient") WebClient userServiceWebClient,
            JwtTokenService jwtTokenService
    ) {
        this.userServiceWebClient = userServiceWebClient;
        this.jwtTokenService = jwtTokenService;
    }

    public String checkJwtUserDecode(Authentication auth) {
        try {
            return userServiceWebClient
                    .get()
                    .uri("/hello")
                    .headers(
                            headers -> headers
                                    .setBearerAuth(jwtTokenService.generateUserToken(
                                                    auth.getName(),
                                                    (List<GrantedAuthority>) auth.getAuthorities(),
                                                    TenantID.COMMON_GROUP.getName()
                                            )
                                    )
                    )
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.out.println();
        }

        return null;
    }
}
