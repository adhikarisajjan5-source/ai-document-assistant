package dev.docmind.exception;

public class NoExtractableTextException extends RuntimeException {

    public NoExtractableTextException() {
        super("No extractable text was found in the PDF");
    }
}