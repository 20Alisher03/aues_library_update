package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.Favorite;
import com.myapp.bookstore.model.User;
import com.myapp.bookstore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndBook(User user, Book book);
    void deleteByUserAndBook(User user, Book book);
}
