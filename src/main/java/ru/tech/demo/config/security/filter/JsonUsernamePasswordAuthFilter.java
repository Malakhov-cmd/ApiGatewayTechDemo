package ru.tech.demo.config.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.tech.demo.config.security.handler.RestAuthHandlers;

import java.io.IOException;

public class JsonUsernamePasswordAuthFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthFilter(RequestMatcher requiresAuth, ObjectMapper objectMapper) {
        super(requiresAuth);
        this.objectMapper = objectMapper;

        setAuthenticationSuccessHandler(RestAuthHandlers.successHandler(objectMapper));
        setAuthenticationFailureHandler(RestAuthHandlers.failureHandler(objectMapper));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        JsonNode node = objectMapper.readTree(request.getInputStream());

        String username = node.path("username").asText("");
        String password = node.path("password").asText("");

        var authRequest = new UsernamePasswordAuthenticationToken(username, password);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // гарантированно создаём HttpSession
        request.getSession(true);

        // Сохраняем Authentication в SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        // Сохраняем SecurityContext в сессии через репозиторий
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, request, response);

        // Не продолжаем цепочку — отвечаем немедленно (success handler отправит JSON и Set-Cookie сделает контейнер)
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
