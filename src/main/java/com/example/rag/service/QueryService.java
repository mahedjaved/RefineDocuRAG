package com.example.rag.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.rag.model.rag.ChatMessage;
import com.example.rag.model.rag.DocumentChunk;
import com.example.rag.repository.ChunkRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {
    private final ChunkRepository chunkRepository;
    private final OllamaService ollamaService;

    public ChatMessage query(String query, Long documentId) throws Exception {
        log.info("Processing query: {}", query);

        List<DocumentChunk> allChunks = documentId != null ? chunkRepository.findByDocumentId(documentId)
                : chunkRepository.findAll();

        List<DocumentChunk> relevantChunks = findRelevantChunks(query, allChunks, 5);

        String context = relevantChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.joining("\n\n"));

        String response = ollamaService.queryWithContext(query, context);

        ChatMessage message = new ChatMessage();
        message.setQuery(query);
        message.setResponse(response);
        message.setSourceChunks(relevantChunks.stream()
                .map(c -> String.format("Chunk %d: %s...",
                        c.getChunkIndex(),
                        c.getContent().substring(0, Math.min(100, c.getContent().length()))))
                .toArray(String[]::new));
        return message;
    }

    private List<DocumentChunk> findRelevantChunks(String query, List<DocumentChunk> chunks, int topK){
        return chunks.stream()
            .sorted((a, b) -> Double.compare(
                calculateRelevance(query, b.getContent()),
                calculateRelevance(query, a.getContent())
            )).limit(topK)
            .collect(Collectors.toList());
    }

    private double calculateRelevance(String query, String text){
        String[] queryWords = query.toLowerCase().split("\\s+");
        String textLower = text.toLowerCase();

        long matchCount = Arrays.stream(queryWords)
            .filter(textLower::contains)
            .count();

        return (double) matchCount / queryWords.length;
    }
}
