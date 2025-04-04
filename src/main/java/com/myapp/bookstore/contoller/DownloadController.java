package com.myapp.bookstore.contoller;

import com.myapp.bookstore.model.Download;
import com.myapp.bookstore.service.DownloadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/downloads")
@CrossOrigin(origins = "http://localhost:5173")
public class DownloadController {
    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping
    public Download addDownload(@RequestBody DownloadRequest downloadRequest) {
        Long userId = downloadRequest.getUserId(); // Получаем userId из запроса
        return downloadService.saveDownload(downloadRequest.getBookId(), userId);
    }

    @GetMapping
    public List<Download> getDownloadsByUser(@RequestParam Long userId) { // Принимаем userId как параметр
        return downloadService.getDownloadsByUserId(userId);
    }

    @DeleteMapping("/{downloadId}")
    public void removeDownload(@PathVariable Long downloadId) {
        downloadService.deleteDownload(downloadId);
    }
}

// Обновленный класс DownloadRequest
class DownloadRequest {
    private Long bookId;
    private Long userId; // Новое поле

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}