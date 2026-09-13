package dev.docmind.dto;

public class TextChunk {

    private final int pageNumber;
    private final int chunkNumber;
    private final String text;

    public TextChunk(int pageNumber, int chunkNumber, String text) {
        this.pageNumber = pageNumber;
        this.chunkNumber = chunkNumber;
        this.text = text;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public String getText() {
        return text;
    }
}