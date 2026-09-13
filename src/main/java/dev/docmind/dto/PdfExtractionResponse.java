package dev.docmind.dto;

import java.util.List;

public class PdfExtractionResponse {

    private final String filename;
    private final int pageCount;
    private final int chunkCount;
    private final List<String> pages;
    private final List<TextChunk> chunks;
    private final List<EmbeddingTestResponse> embeddings;

    public PdfExtractionResponse(
            String filename,
            int pageCount,
            int chunkCount,
            List<String> pages,
            List<TextChunk> chunks,
            List<EmbeddingTestResponse> embeddings
    ) {
        this.filename = filename;
        this.pageCount = pageCount;
        this.chunkCount = chunkCount;
        this.pages = pages;
        this.chunks = chunks;
        this.embeddings = embeddings;
    }

    public String getFilename() {
        return filename;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public List<String> getPages() {
        return pages;
    }

    public List<TextChunk> getChunks() {
        return chunks;
    }

    public List<EmbeddingTestResponse> getEmbeddings() {
        return embeddings;
    }
}