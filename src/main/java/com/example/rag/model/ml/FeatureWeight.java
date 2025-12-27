package com.example.rag.model.ml;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feature_weights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String featureName;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Integer updateCount = 0;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Double minWeight;
    private Double maxWeight;
    private Double averageWeight;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public void updateWeight(double newWeight) {
        this.weight = newWeight;
        this.updateCount++;
        this.lastUpdated = LocalDateTime.now();
        if (this.minWeight == null || newWeight < this.minWeight) {
            this.minWeight = newWeight;
        }
        if (this.maxWeight == null || newWeight > this.maxWeight) {
            this.maxWeight = newWeight;
        }
    }
}
