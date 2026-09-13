package dev.docmind.dto;

public class SearchResultResponse {

    private final String content;
    private final Object documentId;
    private final Object pageNumber;
    private final Object chunkNumber;

    public SearchResultResponse(
            String content,
            Object documentId,
            Object pageNumber,
            Object chunkNumber) {

        this.content = content;
        this.documentId = documentId;
        this.pageNumber = pageNumber;
        this.chunkNumber = chunkNumber;
    }

    public String getContent() {
        return content;
    }

    public Object getDocumentId() {
        return documentId;
    }

    public Object getPageNumber() {
        return pageNumber;
    }

    public Object getChunkNumber() {
        return chunkNumber;
    }
}