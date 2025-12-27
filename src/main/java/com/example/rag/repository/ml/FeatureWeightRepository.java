package com.example.rag.repository.ml;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.rag.model.ml.FeatureWeight;

@Repository
public interface FeatureWeightRepository extends JpaRepository<FeatureWeight, Long> {
    Optional<FeatureWeight> findByFeatureName(String featureName);
    
    @Query("SELECT fw FROM FeatureWeight fw ORDER BY fw.weight DESC")
    List<FeatureWeight> findAllOrderByWeightDesc();
    
    @Query("SELECT fw FROM FeatureWeight fw ORDER BY fw.updateCount DESC")
    List<FeatureWeight> findMostUpdatedFeatures();
    
    @Query("SELECT AVG(fw.weight) FROM FeatureWeight fw")
    Double getAverageWeight();
}
