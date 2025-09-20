package ru.tech.demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tech.demo.aop.BaseMetrics;
import ru.tech.demo.service.user.UserService;

import java.util.Map;

@RestController
@RequestMapping("/usr")
@Tag(name = "User controller", description = "Контроллер получения информации по пользователю")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @BaseMetrics
    public Map<String, Object> getProfileData(Authentication auth) {
        return Map.of(
                "name", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }

    @GetMapping("/check/jwt/user/decode")
    @BaseMetrics
    public String checkJwtUserDecode(Authentication auth) {
        return userService.checkJwtUserDecode(auth);
    }
}
