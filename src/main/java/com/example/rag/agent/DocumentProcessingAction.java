package com.example.rag.agent;

import com.t4a.annotations.Agent;
import com.t4a.annotations.Action;
import com.example.rag.model.rag.Document;
import com.example.rag.service.rag.DocumentProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tools4AI Agent for document management operations
 */
@Agent(groupName = "documentManagement", groupDescription = "AI actions for managing PDF documents")
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingAction {
    private final DocumentProcessingService documentProcessingService;

    @Action(description = "List all uploaded documents")
    public String listDocuments() {
        List<Document> documents = documentProcessingService.getAllDocuments();
        return documents.stream()
                .map(d -> String.format("ID: %d, Name: %s, Chunks: %d, Status: %s",
                        d.getId(), d.getOriginalFilename(),
                        d.getTotalChunks(), d.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    @Action(description = "Get document status and details")
    public String getDocumentInfo(Long documentId) {
        Document doc = documentProcessingService.getDocumentById(documentId);
        if (doc == null) {
            return "Document not found";
        }
        return String.format("Document: %s\nStatus: %s\nChunks: %d\nUploaded: %s",
                doc.getOriginalFilename(), doc.getStatus(),
                doc.getTotalChunks(), doc.getUploadedAt());
    }

    @Action(description = "Get document processing statistics")
    public String getProcessingStats() {
        List<Document> docs = documentProcessingService.getAllDocuments();
        long completed = docs.stream()
                .filter(d -> d.getStatus() == Document.ProcessingStatus.COMPLETED)
                .count();
        long processing = docs.stream()
                .filter(d -> d.getStatus() == Document.ProcessingStatus.PROCESSING)
                .count();
        long failed = docs.stream()
                .filter(d -> d.getStatus() == Document.ProcessingStatus.FAILED)
                .count();

        return String.format("Total Documents: %d\nCompleted: %d\nProcessing: %d\nFailed: %d",
                docs.size(), completed, processing, failed);
    }
}
