package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    // Поиск по ключевому слову в вопросе (игнорируем регистр)
    @Query("SELECT f FROM Faq f WHERE LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Faq> findByQuestionContainingIgnoreCase(@Param("keyword") String keyword);
}
