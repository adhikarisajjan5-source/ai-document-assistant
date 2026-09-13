package dev.docmind.dto;

public class EmbeddingTestResponse {

    private final int chunkNumber;
    private final int pageNumber;
    private final int embeddingDimension;

    public EmbeddingTestResponse(
            int chunkNumber,
            int pageNumber,
            int embeddingDimension
    ) {
        this.chunkNumber = chunkNumber;
        this.pageNumber = pageNumber;
        this.embeddingDimension = embeddingDimension;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }
}