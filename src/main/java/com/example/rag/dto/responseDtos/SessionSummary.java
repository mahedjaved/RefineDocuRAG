package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionSummary {
    private String sessionId;
    private String originalPrompt;
    private String refinedPrompt;
    private Integer iterations;
    private Boolean converged;
    private Double finalScore;
    private String createdAt;
    private String method;
}