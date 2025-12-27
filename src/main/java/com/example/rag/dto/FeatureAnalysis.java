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
public class FeatureAnalysis {
    private Map<String, Double> features;
    private Map<String, Double> weights;
    private Double totalScore;
    private List<String> strengths;
    private List<String> weaknesses;
    private String recommendations;
}
