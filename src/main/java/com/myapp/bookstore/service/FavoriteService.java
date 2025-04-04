package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Favorite;
import com.myapp.bookstore.model.User;
import com.myapp.bookstore.model.Book;
import com.myapp.bookstore.repository.FavoriteRepository;
import com.myapp.bookstore.repository.BookRepository;
import com.myapp.bookstore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<Favorite> getUserFavorites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));
        return favoriteRepository.findByUser(user);
    }

    @Transactional
    public Favorite addToFavorites(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book with ID " + bookId + " not found"));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndBook(user, book);
        if (existingFavorite.isPresent()) {
            throw new RuntimeException("Book is already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book with ID " + bookId + " not found"));

        Optional<Favorite> favorite = favoriteRepository.findByUserAndBook(user, book);
        if (favorite.isEmpty()) {
            throw new RuntimeException("Favorite entry for user " + userId + " and book " + bookId + " not found");
        }

        favoriteRepository.deleteByUserAndBook(user, book);
    }
}