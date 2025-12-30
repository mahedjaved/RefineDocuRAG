package com.example.rag.controller.ml;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rag.dto.requestDtos.RefinementRequest;
import com.example.rag.dto.responseDtos.RefinementResponse;
import com.example.rag.service.ml.PromptRefinementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/refinement")
@Slf4j
@RequiredArgsConstructor
public class RefinementController {

    private final PromptRefinementService promptRefinementService;

    @PostMapping("/refine")
    public ResponseEntity<?> refinePrompt(@RequestBody RefinementRequest request) {
        try {
            log.info("Received refinement request for prompt: {}",
                    request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())));

            // validate the prompt
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Prompt cannot be empty"));
            }

            RefinementResponse response = promptRefinementService.refinePrompt(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error refining prompt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to refine prompt: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Prompt Refinement Service",
                "timestamp", System.currentTimeMillis()));
    }
}
