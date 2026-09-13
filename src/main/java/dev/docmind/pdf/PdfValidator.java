package dev.docmind.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PdfValidator {

    public void validate(byte[] pdfBytes) {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException(
                    "PDF file must not be empty"
            );
        }

        validatePdfSignature(pdfBytes);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException(
                        "PDF must contain at least one page"
                );
            }

        } catch (IOException exception) {

            throw new IllegalArgumentException(
                    "Invalid or corrupted PDF file"
            );
        }
    }

    private void validatePdfSignature(byte[] pdfBytes) {

        if (pdfBytes.length < 5) {
            throw new IllegalArgumentException(
                    "Invalid PDF file"
            );
        }

        String header = new String(
                pdfBytes,
                0,
                5,
                StandardCharsets.US_ASCII
        );

        if (!"%PDF-".equals(header)) {
            throw new IllegalArgumentException(
                    "Invalid PDF file"
            );
        }
    }
}