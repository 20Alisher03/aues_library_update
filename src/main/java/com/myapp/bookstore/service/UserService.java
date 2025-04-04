package com.myapp.bookstore.service;

import com.myapp.bookstore.model.User;
import com.myapp.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Transactional
    public User registerUser(User user) {
        // Проверяем уникальность username и email
        if (userRepository.findByUsernameIgnoreCase(user.getUsername()) != null) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        // Пароль сохраняется в plaintext (без хеширования)
        // Преобразуем birthDate из строки, если передана
        if (user.getBirthDate() != null && user.getBirthDate().toString().length() == 10) {
            user.setBirthDate(LocalDate.parse(user.getBirthDate().toString()));
        }

        user.setIsVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        User savedUser = userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        return savedUser;
    }

    public boolean authenticateUser(String username, String password) {
        User user = userRepository.findByUsernameIgnoreCase(username);
        if (user != null) {
            // Проверка пароля в plaintext
            return user.getPassword().equals(password);
        }
        return false;
    }

    public User verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token);
        if (user != null) {
            user.setIsVerified(true);
            user.setVerificationToken(null);
            return userRepository.save(user);
        }
        return null;
    }
}