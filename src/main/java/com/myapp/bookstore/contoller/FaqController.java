package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.Faq;
import com.myapp.bookstore.repository.FaqRepository;
import com.myapp.bookstore.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@CrossOrigin(origins = "http://localhost:5173")
public class FaqController {

    @Autowired
    private FaqService faqService;

    @Autowired
    private FaqRepository faqRepository;

    @GetMapping
    public List<Faq> getAllFaqs() {
        return faqService.getAllFaqs();
    }

    @GetMapping("/search")
    public List<Faq> searchFaqs(@RequestParam String keyword) {
        return faqService.searchFaqs(keyword);
    }

    @PostMapping
    public ResponseEntity<Faq> addFaq(@RequestBody Faq faq) {
        Faq savedFaq = faqService.addFaq(faq);
        return ResponseEntity.ok(savedFaq);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFaq(@PathVariable Long id) {
        if (faqRepository.existsById(id)) {
            faqRepository.deleteById(id);
            return ResponseEntity.ok("FAQ удалён");
        }
        return ResponseEntity.badRequest().body("FAQ не найден");
    }
}