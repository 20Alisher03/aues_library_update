package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.Book;
import com.myapp.bookstore.repository.BookRepository;
import com.myapp.bookstore.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {
    private final BookService bookService;
    private final BookRepository bookRepository;

    public BookController(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) Integer ratings) {
        System.out.println("Параметры поиска: search=" + search + ", field=" + field + ", sort=" + sort +
                ", genre=" + genre + ", language=" + language + ", age=" + age + ", ratings=" + ratings);

        List<String> genres = (genre != null && !genre.trim().isEmpty()) ?
                Arrays.stream(genre.split(",")).map(String::trim).collect(Collectors.toList()) : null;
        List<String> languages = (language != null && !language.trim().isEmpty()) ?
                Arrays.stream(language.split(",")).map(String::trim).collect(Collectors.toList()) : null;
        List<String> ages = (age != null && !age.trim().isEmpty()) ?
                Arrays.stream(age.split(",")).map(String::trim).collect(Collectors.toList()) : null;

        return bookService.getAllBooks(search, field, sort, genres, languages, ages, ratings);
    }

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.ok("Книга удалена");
        }
        return ResponseEntity.badRequest().body("Книга не найдена");
    }
}