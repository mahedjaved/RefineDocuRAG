package com.example.rag.dto.responseDtos;

import java.util.Map;

import lombok.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegressionResult {
    private String method;
    private Double mse;
    private Double rmse;
    private Double mae;
    private Double rSquared;
    private Map<String, Double> featureImportance;
    private Integer trainingDataSize;
    private String status;
}
