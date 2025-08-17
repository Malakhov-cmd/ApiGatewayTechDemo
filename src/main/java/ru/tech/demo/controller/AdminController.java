package ru.tech.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello API", description = "Пример HelloWorld API")
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/hello")
    @Operation(summary = "Приветственное сообщение", description = "Возвращает Hello World")
    public String hello() {
        log.debug("Calling hello endpoint");
        return "Hello, World!";
    }
}
