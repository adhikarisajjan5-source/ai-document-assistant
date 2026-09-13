package dev.docmind.repository;

import dev.docmind.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    boolean existsByIdAndOwnerId(
            Long documentId,
            Long ownerId
    );

    List<Document> findByOwnerId(Long ownerId);
}