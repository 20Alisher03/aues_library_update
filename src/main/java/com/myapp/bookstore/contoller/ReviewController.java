package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.Book;
import com.myapp.bookstore.model.Review;
import com.myapp.bookstore.model.User;
import com.myapp.bookstore.repository.BookRepository;
import com.myapp.bookstore.repository.ReviewRepository;
import com.myapp.bookstore.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReviewController(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest reviewRequest) {
        try {
            Book book = bookRepository.findById(reviewRequest.getBookId())
                    .orElseThrow(() -> new RuntimeException("Книга не найдена"));
            User user = userRepository.findById(reviewRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Review review = new Review();
            review.setBook(book);
            review.setUser(user);
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());
            review.setCreatedAt(LocalDateTime.now());

            Review savedReview = reviewRepository.save(review);
            return ResponseEntity.ok(savedReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBookId(@PathVariable Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return ResponseEntity.ok("Отзыв удалён");
        }
        return ResponseEntity.badRequest().body("Отзыв не найден");
    }
}

class ReviewRequest {
    private Long bookId;
    private Long userId;
    private int rating;
    private String comment;

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}