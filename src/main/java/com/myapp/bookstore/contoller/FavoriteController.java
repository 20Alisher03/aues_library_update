package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.Favorite;
import com.myapp.bookstore.model.Book;
import com.myapp.bookstore.service.FavoriteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173") // Разрешаем CORS для Vue.js
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Теперь метод возвращает список книг, а не Favorite
    @GetMapping("/{userId}")
    public ResponseEntity<List<Book>> getUserFavorites(@PathVariable Long userId) {
        List<Book> favoriteBooks = favoriteService.getUserFavorites(userId)
                .stream()
                .map(Favorite::getBook) // Получаем книги из избранного
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoriteBooks);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> addToFavorites(@PathVariable Long userId, @RequestBody Map<String, Long> request) {
        Long bookId = request.get("bookId");
        if (bookId == null) {
            return ResponseEntity.badRequest().body("bookId is required");
        }
        return ResponseEntity.ok(favoriteService.addToFavorites(userId, bookId));
    }

    @DeleteMapping("/{userId}/{bookId}")
    public ResponseEntity<?> removeFromFavorites(@PathVariable Long userId, @PathVariable Long bookId) {
        favoriteService.removeFromFavorites(userId, bookId);
        return ResponseEntity.ok().build();
    }
}



