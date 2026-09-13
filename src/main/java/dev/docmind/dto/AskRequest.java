package dev.docmind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AskRequest {

    @NotNull(message = "Document ID is required")
    @Positive(message = "Document ID must be greater than zero")
    private Long documentId;

    @NotBlank(message = "Query must not be empty")
    private String query;

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}