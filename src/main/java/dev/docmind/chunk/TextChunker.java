package dev.docmind.chunk;

import dev.docmind.dto.TextChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private static final int MAX_CHUNK_SIZE = 500;

    public List<TextChunk> chunkPages(List<String> pages) {

        List<TextChunk> chunks = new ArrayList<>();
        int chunkNumber = 1;

        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {

            String pageText = pages.get(pageIndex);

            if (pageText == null || pageText.isBlank()) {
                continue;
            }

            // Remove unnecessary line breaks and repeated spaces
            String cleanedText = pageText
                    .replaceAll("\\s+", " ")
                    .trim();

            // Split text at sentence boundaries
            String[] sentences =
                    cleanedText.split("(?<=[.!?])\\s+");

            StringBuilder currentChunk = new StringBuilder();

            for (String sentence : sentences) {

                if (sentence.isBlank()) {
                    continue;
                }

                // If adding this sentence makes the chunk too large,
                // save the current chunk first.
                if (!currentChunk.isEmpty()
                        && currentChunk.length() + sentence.length() + 1
                        > MAX_CHUNK_SIZE) {

                    chunks.add(
                            new TextChunk(
                                    pageIndex + 1,
                                    chunkNumber++,
                                    currentChunk.toString().trim()
                            )
                    );

                    currentChunk = new StringBuilder();
                }

                if (!currentChunk.isEmpty()) {
                    currentChunk.append(" ");
                }

                currentChunk.append(sentence);
            }

            // Save remaining text
            if (!currentChunk.isEmpty()) {

                chunks.add(
                        new TextChunk(
                                pageIndex + 1,
                                chunkNumber++,
                                currentChunk.toString().trim()
                        )
                );
            }
        }

        return chunks;
    }
}