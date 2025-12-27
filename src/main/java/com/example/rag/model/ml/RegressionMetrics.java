package com.example.rag.model.ml;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regression_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegressionMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    // Mean squared errors
    @Column(nullable = false)
    private Double mse;

    // Root mean squared
    @Column(nullable = false)
    private Double rmse;

    // Mean absolute error
    @Column(nullable = false)
    private Double mae;

    // R-squared
    @Column(nullable = false)
    private Double rSquared;

    private Integer trainingDataSize;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @ElementCollection
    @CollectionTable(name = "feature_importance", joinColumns = @JoinColumn(name = "metrics_id"))
    @MapKeyColumn(name = "feature_name")
    @Column(name = "importance_value")
    private Map<String, Double> featureImportance = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
}