package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Book;
import com.myapp.bookstore.repository.BookRepository;
import com.myapp.bookstore.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<Book> getAllBooks(String search, String field, String sort,
                                  List<String> genres, List<String> languages,
                                  List<String> ages, Integer ratings) {
        List<Book> books = bookRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            if ("author".equals(field)) {
                books = bookRepository.findByAuthorContainingIgnoreCase(search);
            } else {
                books = bookRepository.findByTitleContainingIgnoreCase(search);
            }
        }

        if (genres != null && !genres.isEmpty()) {
            books = bookRepository.findByGenreIn(genres);
        }

        if (languages != null && !languages.isEmpty()) {
            books = bookRepository.findByLanguageIn(languages);
        }

        if (ages != null && !ages.isEmpty()) {
            books = bookRepository.findByAgeIn(ages);
        }

        for (Book book : books) {
            Double avgRating = reviewRepository.calculateAverageRatingByBookId(book.getId());
            book.setRatings(avgRating != null ? avgRating : 0.0);
        }

        if (ratings != null) {
            books = books.stream()
                    .filter(book -> Math.round(book.getRatings()) == ratings)
                    .collect(Collectors.toList());
        }

        if (sort != null && !sort.trim().isEmpty()) {
            switch (sort) {
                case "title" -> books.sort(Comparator.comparing(Book::getTitle));
                case "-year" -> books.sort(Comparator.comparing(Book::getYear).reversed());
                case "year" -> books.sort(Comparator.comparing(Book::getYear));
                default -> System.out.println("Неизвестный параметр сортировки: " + sort);
            }
        }

        return books;
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public boolean existsById(Long id) {
        return bookRepository.existsById(id);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}