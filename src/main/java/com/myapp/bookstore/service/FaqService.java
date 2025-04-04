package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Faq;
import com.myapp.bookstore.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    // Получить все вопросы и ответы
    public List<Faq> getAllFaqs() {
        return faqRepository.findAll();
    }

    // Поиск по ключевому слову
    public List<Faq> searchFaqs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllFaqs();
        }
        return faqRepository.findByQuestionContainingIgnoreCase(keyword);
    }

    // Добавить новый FAQ (для админки, если нужно)
    public Faq addFaq(Faq faq) {
        return faqRepository.save(faq);
    }
}
