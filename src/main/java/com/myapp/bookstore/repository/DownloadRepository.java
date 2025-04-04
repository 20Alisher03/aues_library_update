package com.myapp.bookstore.repository;

import com.myapp.bookstore.model.Download;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownloadRepository extends JpaRepository<Download, Long> {
    boolean existsByBookIdAndUserId(Long bookId, Long userId); // Обновляем проверку
    List<Download> findByUserId(Long userId); // Новый метод для получения скачиваний пользователя
}