package ru.tech.demo.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestAuthHandlers {

    public static AuthenticationSuccessHandler successHandler(ObjectMapper om) {
        return (request, response, authentication) -> {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            om.writeValue(response.getWriter(), Map.of(
                    "status", "ok",
                    "user", authentication.getName()
            ));
        };
    }

    public static AuthenticationFailureHandler failureHandler(ObjectMapper om) {
        return (request, response, ex) -> writeAuthError(response, om, ex);
    }

    private static void writeAuthError(HttpServletResponse response, ObjectMapper om, AuthenticationException ex) throws java.io.IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        om.writeValue(response.getWriter(), Map.of(
                "status", "error",
                "error", ex.getClass().getSimpleName(),
                "message", String.valueOf(ex.getMessage())
        ));
    }
}
