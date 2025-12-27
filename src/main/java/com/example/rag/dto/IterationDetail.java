package com.example.rag.dto;

import java.util.Map;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IterationDetail {
    private Integer iteration;
    private String prompt;
    private Double qualityScore;
    private Double predictedScore;
    private String feedback;
    private Map<String, Double> features;
    private Double improvement;
}