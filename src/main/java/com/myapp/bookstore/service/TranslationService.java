package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Translation;
import com.myapp.bookstore.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslationService {

    @Autowired
    private TranslationRepository translationRepository;

    public Map<String, String> getTranslations(String language) {
        List<Translation> translations = translationRepository.findByLanguage(language);
        Map<String, String> translationMap = new HashMap<>();
        for (Translation translation : translations) {
            translationMap.put(translation.getKey(), translation.getValue());
        }
        System.out.println("Translations for language " + language + ": " + translationMap); // Логирование
        return translationMap;
    }
}