package com.example.rag.service.rag;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        String[] sentences = text.split("(?<=[.!?])\\\\s+");
        // store chunks using String builder
        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;

        for (String sentence : sentences) {
            int sentenceLength = sentence.length();

            if (currentSize + sentenceLength > chunkSize && currentSize > 0) {

                chunks.add(currentChunk.toString().trim());
                int overlapStart = Math.max(0, currentChunk.length() - overlap);
                currentChunk = new StringBuilder(currentChunk.substring(overlapStart));
                currentSize = currentChunk.length();

            }

            currentChunk.append(sentence).append(" ");
            currentSize += sentenceLength + 1;
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }

    public List<String> chunkTextByCharacters(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
        }
        return chunks;
    }
}
