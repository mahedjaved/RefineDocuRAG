package com.example.rag.agent;

import com.t4a.annotations.Agent;
import com.t4a.annotations.Action;
import com.example.rag.model.ChatMessage;
import com.example.rag.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Tools4AI Agent for RAG operations
 * This class demonstrates integration with the Tools4AI agentic framework
 */
@Agent(groupName = "documentQuery", groupDescription = "AI actions for querying documents using RAG")
@Component
@RequiredArgsConstructor
@Slf4j
public class RAGAction {
    private final QueryService queryService;

    @Action(description = "Search and answer questions from uploaded documents")
    public String queryDocuments(String question, Long documentId) {
        try {
            log.info("RAG Action triggered for question: {}", question);
            ChatMessage response = queryService.query(question, documentId);
            return response.getResponse();
        } catch (Exception e) {
            log.error("Error in RAG action", e);
            return "Error processing query: " + e.getMessage();
        }
    }

    @Action(description = "Search across all documents")
    public String searchAllDocuments(String query) {
        return queryDocuments(query, null);
    }

    @Action(description = "Get summary of a specific document chunk")
    public String summarizeChunk(String chunkContent) {
        try {
            ChatMessage response = queryService.query(
                    "Provide a brief summary of this text", null);
            return response.getResponse();
        } catch (Exception e) {
            return "Error generating summary: " + e.getMessage();
        }
    }
}
