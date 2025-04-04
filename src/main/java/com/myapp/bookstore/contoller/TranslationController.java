package com.myapp.bookstore.contoller;

import com.myapp.bookstore.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @GetMapping
    public Map<String, String> getTranslations(@RequestHeader(value = "Accept-Language", defaultValue = "ru") String language) {
        System.out.println("Received request for language: " + language); // Логирование
        return translationService.getTranslations(language);
    }
}