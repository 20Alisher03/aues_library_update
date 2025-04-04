package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenreIn(List<String> genres);
    List<Book> findByLanguageIn(List<String> languages);
    List<Book> findByAgeIn(List<String> ages);
    // Удален метод: List<Book> findByRatings(int ratings);
}