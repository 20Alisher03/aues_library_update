package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameIgnoreCase(String username);
    User findByEmail(String email);
    User findByVerificationToken(String verificationToken); // Добавляем этот метод
}