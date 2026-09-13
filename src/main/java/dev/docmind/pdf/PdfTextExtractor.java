package dev.docmind.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfTextExtractor {

    public List<String> extractTextByPage(byte[] pdfBytes) throws IOException {

        List<String> pages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            int totalPages = document.getNumberOfPages();

            for (int pageNumber = 1; pageNumber <= totalPages; pageNumber++) {

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String text = stripper.getText(document);

                pages.add(text);
            }
        }

        return pages;
    }
}