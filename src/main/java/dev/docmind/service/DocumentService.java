package dev.docmind.service;

import dev.docmind.chunk.TextChunker;
import dev.docmind.dto.DocumentResponse;
import dev.docmind.dto.TextChunk;
import dev.docmind.entity.Document;
import dev.docmind.entity.DocumentStatus;
import dev.docmind.entity.User;
import dev.docmind.exception.NoExtractableTextException;
import dev.docmind.pdf.PdfTextExtractor;
import dev.docmind.pdf.PdfValidator;
import dev.docmind.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final PdfValidator pdfValidator;
    private final TextChunker textChunker;

    public DocumentService(
            DocumentRepository documentRepository,
            PdfTextExtractor pdfTextExtractor,
            PdfValidator pdfValidator,
            TextChunker textChunker) {

        this.documentRepository = documentRepository;
        this.pdfTextExtractor = pdfTextExtractor;
        this.pdfValidator = pdfValidator;
        this.textChunker = textChunker;
    }

    @Transactional
    public Document uploadDocument(
            String filename,
            User owner) {

        Document document =
                new Document(
                        filename,
                        DocumentStatus.PROCESSING,
                        owner
                );

        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByOwner(
            Long ownerId) {

        return documentRepository
                .findByOwnerId(ownerId)
                .stream()
                .map(document ->
                        new DocumentResponse(
                                document.getId(),
                                document.getOriginalFilename(),
                                document.getStatus(),
                                document.getCreatedAt()
                        )
                )
                .toList();
    }

    public void validatePdf(byte[] pdfBytes) {

        pdfValidator.validate(pdfBytes);
    }

    public List<String> extractPdfText(byte[] pdfBytes)
            throws IOException {

        return pdfTextExtractor.extractTextByPage(pdfBytes);
    }

    public List<TextChunk> chunkPdfPages(
            List<String> pages) {

        List<TextChunk> chunks =
                textChunker.chunkPages(pages);

        if (chunks.isEmpty()) {
            throw new NoExtractableTextException();
        }

        return chunks;
    }

    @Transactional(readOnly = true)
    public boolean existsByIdAndOwnerId(
            Long documentId,
            Long ownerId) {

        return documentRepository
                .existsByIdAndOwnerId(
                        documentId,
                        ownerId
                );
    }

    @Transactional
    public Document updateStatus(
            Document document,
            DocumentStatus status) {

        document.setStatus(status);

        return documentRepository.save(document);
    }
}