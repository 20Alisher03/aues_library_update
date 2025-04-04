package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findByLanguage(String language);
}
