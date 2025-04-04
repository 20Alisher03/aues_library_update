package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.User;
import com.myapp.bookstore.repository.UserRepository;
import com.myapp.bookstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("API работает! Доступны эндпоинты /api/register, /api/login, /api/confirm, /api/profile");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok().body("{\"message\":\"Регистрация успешна\",\"user_id\":" + savedUser.getId() + "}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginUser) {
        try {
            if (!userService.authenticateUser(loginUser.getUsername(), loginUser.getPassword())) {
                return ResponseEntity.badRequest().body("{\"error\":\"Неверные учетные данные\"}");
            }

            User user = userRepository.findByUsernameIgnoreCase(loginUser.getUsername());
            return ResponseEntity.ok().body("{\"message\":\"Вход выполнен успешно\",\"user_id\":" + user.getId() + ",\"user_data\":" + getUserJson(user) + ",\"role\":\"" + (user.getRole() != null ? user.getRole() : "USER") + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        User user = userService.verifyUser(token);
        if (user != null) {
            return ResponseEntity.ok("Email успешно подтвержден!");
        } else {
            return ResponseEntity.badRequest().body("Неверный или устаревший токен подтверждения.");
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));
        return ResponseEntity.ok(getUserJson(user));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<String> updateProfile(@PathVariable Long userId, @RequestBody User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с ID: " + userId));

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setMiddleName(userDetails.getMiddleName());
        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());
        user.setBirthDate(userDetails.getBirthDate());

        userRepository.save(user);
        return ResponseEntity.ok("{\"message\":\"Профиль успешно обновлен\"}");
    }

    // Вспомогательный метод для конвертации User в JSON
    private String getUserJson(User user) {
        return "{\"id\":" + user.getId() +
                ",\"username\":\"" + user.getUsername() + "\"" +
                ",\"firstName\":\"" + (user.getFirstName() != null ? user.getFirstName() : "") + "\"" +
                ",\"lastName\":\"" + (user.getLastName() != null ? user.getLastName() : "") + "\"" +
                ",\"middleName\":\"" + (user.getMiddleName() != null ? user.getMiddleName() : "") + "\"" +
                ",\"phone\":\"" + (user.getPhone() != null ? user.getPhone() : "") + "\"" +
                ",\"email\":\"" + (user.getEmail() != null ? user.getEmail() : "") + "\"" +
                ",\"birth_date\":\"" + (user.getBirthDate() != null ? user.getBirthDate().toString() : "") + "\"}";
    }
}