package com.example.rag.dto.requestDtos;

import java.util.List;
import java.util.Map;

import lombok.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefinementRequest {
    private String prompt;
    private Integer maxIterations = 5;
    private Double convergenceThreshold = 0.95;
    private String regressionMethod = "ENSEMBLE"; // LINEAR, POLYNOMIAL, NEURAL, ENSEMBLE
    private List<String> optimizationGoals; // CLARITY, RELEVANCE, COMPLETENESS, SPECIFICITY
    private Map<String, Double> featureWeights; // Optional custom weights

}