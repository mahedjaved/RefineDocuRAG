package com.example.rag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.rag.model.rag.Document;
import com.example.rag.model.rag.DocumentChunk;
import com.example.rag.service.rag.DocumentProcessingService;

import kong.unirest.core.HttpStatus;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (!file.getOriginalFilename().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are allowed");
            }

            Document document = documentProcessingService.processDocument(file);
            return ResponseEntity.ok(document);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.toString());
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        return ResponseEntity.ok(documentProcessingService.getAllDocuments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        Document document = documentProcessingService.getDocumentById(id);
        return document != null
                ? ResponseEntity.ok(document)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<DocumentChunk>> getDocumentChunks(@PathVariable Long id) {
        return ResponseEntity.ok(documentProcessingService.getDocumentChunks(id));
    }
}