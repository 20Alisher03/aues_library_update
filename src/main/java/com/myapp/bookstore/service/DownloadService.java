package com.myapp.bookstore.service;

import com.myapp.bookstore.model.Download;
import com.myapp.bookstore.repository.DownloadRepository;
import com.myapp.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DownloadService {
    private final DownloadRepository downloadRepository;
    private final BookRepository bookRepository;

    public DownloadService(DownloadRepository downloadRepository, BookRepository bookRepository) {
        this.downloadRepository = downloadRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Download saveDownload(Long bookId, Long userId) {
        // Проверяем, существует ли книга
        bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Книга с ID " + bookId + " не найдена"));

        // Проверяем, не была ли книга уже скачана этим пользователем
        if (downloadRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new RuntimeException("Книга уже скачана этим пользователем");
        }

        Download download = new Download(bookId, userId, LocalDateTime.now());
        return downloadRepository.save(download);
    }

    public List<Download> getDownloadsByUserId(Long userId) {
        return downloadRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteDownload(Long downloadId) {
        Download download = downloadRepository.findById(downloadId)
                .orElseThrow(() -> new RuntimeException("Скачивание с ID " + downloadId + " не найдено"));
        downloadRepository.delete(download);
    }
}