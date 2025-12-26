package com.example.rag.controller;

import com.example.rag.model.ChatMessage;
import com.example.rag.service.QueryService;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final QueryService queryService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody QueryRequest request) {
        try {
            ChatMessage response = queryService.query(
                    request.getQuery(),
                    request.getDocumentId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing query: " + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class QueryRequest {
        private String query;
        private Long documentId;
    }
}