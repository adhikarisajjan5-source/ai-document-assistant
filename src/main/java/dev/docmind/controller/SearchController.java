package dev.docmind.controller;

import dev.docmind.dto.AskRequest;
import dev.docmind.dto.SearchResultResponse;
import dev.docmind.entity.User;
import dev.docmind.exception.DocumentNotFoundException;
import dev.docmind.service.CurrentUserService;
import dev.docmind.service.DocumentService;
import dev.docmind.service.VectorStoreService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final VectorStoreService vectorStoreService;
    private final DocumentService documentService;
    private final CurrentUserService currentUserService;

    public SearchController(
            VectorStoreService vectorStoreService,
            DocumentService documentService,
            CurrentUserService currentUserService) {

        this.vectorStoreService = vectorStoreService;
        this.documentService = documentService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public List<SearchResultResponse> search(
            @Valid @RequestBody AskRequest request) {

        User currentUser =
                currentUserService.getCurrentUser();

        boolean ownsDocument =
                documentService.existsByIdAndOwnerId(
                        request.getDocumentId(),
                        currentUser.getId()
                );

        if (!ownsDocument) {
            throw new DocumentNotFoundException(
                    request.getDocumentId()
            );
        }

        return vectorStoreService.search(
                request.getDocumentId(),
                currentUser.getId(),
                request.getQuery()
        );
    }
}