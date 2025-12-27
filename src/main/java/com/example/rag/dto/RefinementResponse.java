package com.example.rag.dto;

import java.util.List;
import java.util.Map;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefinementResponse {
    private String sessionId;
    private String originalPrompt;
    private String refinedPrompt;
    private Integer totalIterations;
    private Boolean converged;
    private Double finalScore;
    private Double initialScore;
    private Double improvementPercentage;
    private List<IterationDetail> iterations;
    private RegressionResult RegressionResult;
    private Map<String, Double> finalFeatures;
    private String regressionMethod;
}
