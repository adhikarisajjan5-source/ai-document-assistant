package dev.docmind.dto;

import dev.docmind.entity.DocumentStatus;

public class DocumentUploadResponse {

    private final Long documentId;
    private final String filename;
    private final DocumentStatus status;
    private final int pageCount;
    private final int chunkCount;

    public DocumentUploadResponse(
            Long documentId,
            String filename,
            DocumentStatus status,
            int pageCount,
            int chunkCount) {

        this.documentId = documentId;
        this.filename = filename;
        this.status = status;
        this.pageCount = pageCount;
        this.chunkCount = chunkCount;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getFilename() {
        return filename;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }
}