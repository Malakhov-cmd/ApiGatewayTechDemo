package ru.tech.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {
    @GetMapping("/")
    public String redirect() {
        return "redirect:/swagger-ui.html";
    }
}
