package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureWeightInfo {
    private String featureName;
    private Double weight;
    private Integer updateCount;
    private String lastUpdated;
}