package com.example.rag.dto.responseDtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsSummary {
    private String method;
    private Double averageMse;
    private Double averageRmse;
    private Double averageMae;
    private Double averageRSquared;
    private Integer totalSessions;
}