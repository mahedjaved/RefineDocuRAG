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
