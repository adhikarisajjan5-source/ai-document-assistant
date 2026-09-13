package dev.docmind.dto;

import dev.docmind.entity.DocumentStatus;

import java.time.LocalDateTime;

public class DocumentResponse {

    private final Long id;
    private final String originalFilename;
    private final DocumentStatus status;
    private final LocalDateTime createdAt;

    public DocumentResponse(
            Long id,
            String originalFilename,
            DocumentStatus status,
            LocalDateTime createdAt) {

        this.id = id;
        this.originalFilename = originalFilename;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}