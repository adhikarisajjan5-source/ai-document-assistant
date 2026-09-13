package dev.docmind.controller;

import dev.docmind.dto.DocumentResponse;
import dev.docmind.dto.DocumentUploadResponse;
import dev.docmind.dto.TextChunk;
import dev.docmind.entity.Document;
import dev.docmind.entity.DocumentStatus;
import dev.docmind.entity.User;
import dev.docmind.service.CurrentUserService;
import dev.docmind.service.DocumentService;
import dev.docmind.service.VectorStoreService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final VectorStoreService vectorStoreService;
    private final CurrentUserService currentUserService;

    public DocumentController(
            DocumentService documentService,
            VectorStoreService vectorStoreService,
            CurrentUserService currentUserService) {

        this.documentService = documentService;
        this.vectorStoreService = vectorStoreService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/upload")
    public DocumentUploadResponse uploadDocument(
            @RequestParam("file") MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("application/pdf")
                        && !contentType.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        byte[] pdfBytes = file.getBytes();

        documentService.validatePdf(pdfBytes);

        User owner =
                currentUserService.getCurrentUser();

        Document savedDocument =
                documentService.uploadDocument(
                        filename,
                        owner
                );

        try {

            List<String> pages =
                    documentService.extractPdfText(pdfBytes);

            List<TextChunk> chunks =
                    documentService.chunkPdfPages(pages);

            vectorStoreService.storeChunks(
                    savedDocument.getId(),
                    owner.getId(),
                    chunks
            );

            Document readyDocument =
                    documentService.updateStatus(
                            savedDocument,
                            DocumentStatus.READY
                    );

            return new DocumentUploadResponse(
                    readyDocument.getId(),
                    readyDocument.getOriginalFilename(),
                    readyDocument.getStatus(),
                    pages.size(),
                    chunks.size()
            );

        } catch (Exception exception) {

            documentService.updateStatus(
                    savedDocument,
                    DocumentStatus.FAILED
            );

            throw exception;
        }
    }

    @GetMapping
    public List<DocumentResponse> getDocuments() {

        User currentUser =
                currentUserService.getCurrentUser();

        return documentService.getDocumentsByOwner(
                currentUser.getId()
        );
    }
}