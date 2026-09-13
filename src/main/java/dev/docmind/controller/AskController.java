package dev.docmind.controller;

import dev.docmind.dto.AskRequest;
import dev.docmind.dto.AskResponse;
import dev.docmind.dto.SearchRequest;
import dev.docmind.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ask")
public class AskController {

    private final RagService ragService;

    public AskController(RagService ragService){
        this.ragService = ragService;
    }

    @PostMapping
    public AskResponse ask(
            @Valid @RequestBody AskRequest request){

        if (request.getDocumentId() == null) {
            throw new IllegalArgumentException(
                    "Document ID is required"
            );
        }

        if(request.getQuery() == null ||
                request.getQuery().isBlank()){
            throw new IllegalArgumentException(
                    "Question must not be empty"
            );
        }

        return ragService.ask(request.getDocumentId(),
                request.getQuery());
    }
}
