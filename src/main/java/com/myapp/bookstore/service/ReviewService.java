package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Review;
import com.myapp.bookstore.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review updateReview(Review review) {
        Review existing = reviewRepository.findById(review.getId())
                .orElseThrow(() -> new RuntimeException("Review not found"));
        existing.setRating(review.getRating());
        existing.setComment(review.getComment());
        return reviewRepository.save(existing);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}