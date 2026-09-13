package dev.docmind.service;

import dev.docmind.dto.SearchResultResponse;
import dev.docmind.dto.TextChunk;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VectorStoreService {

    private static final int TOP_K = 3;

    private final VectorStore vectorStore;

    public VectorStoreService(
            VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }

    public void storeChunks(
            Long documentId,
            Long ownerId,
            List<TextChunk> chunks) {

        List<Document> vectorDocuments =
                new ArrayList<>();

        for (TextChunk chunk : chunks) {

            Map<String, Object> metadata =
                    Map.of(
                            "documentId", documentId,
                            "ownerId", ownerId,
                            "pageNumber", chunk.getPageNumber(),
                            "chunkNumber", chunk.getChunkNumber()
                    );

            Document vectorDocument =
                    new Document(
                            chunk.getText(),
                            metadata
                    );

            vectorDocuments.add(vectorDocument);
        }

        vectorStore.add(vectorDocuments);
    }

    public List<SearchResultResponse> search(
            Long documentId,
            Long ownerId,
            String query) {

        String filterExpression =
                "documentId == " + documentId
                        + " && ownerId == " + ownerId;

        SearchRequest searchRequest =
                SearchRequest.builder()
                        .query(query)
                        .topK(TOP_K)
                        .filterExpression(filterExpression)
                        .build();

        List<Document> results =
                vectorStore.similaritySearch(
                        searchRequest
                );

        return results
                .stream()
                .map(document ->
                        new SearchResultResponse(
                                document.getText(),
                                document.getMetadata()
                                        .get("documentId"),
                                document.getMetadata()
                                        .get("pageNumber"),
                                document.getMetadata()
                                        .get("chunkNumber")
                        )
                )
                .toList();
    }
}