package com.example.rag.repository.ml;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.rag.model.ml.RegressionMetrics;

@Repository
public interface RegressionMetricsRepository extends JpaRepository<RegressionMetrics, Long> {
    List<RegressionMetrics> findBySessionId(String sessionId);

    List<RegressionMetrics> findByRegressionMethod(String method);

    @Query("SELECT rm FROM RegressionMetrics rm WHERE rm.sessionId = :sessionId ORDER BY rm.calculatedAt DESC")
    List<RegressionMetrics> findBySessionIdOrderByCalculatedAtDesc(@Param("sessionId") String sessionId);

    @Query("SELECT AVG(rm.rSquared) FROM RegressionMetrics rm WHERE rm.regressionMethod = :method")
    Double getAverageRSquaredByMethod(@Param("method") String method);

    @Query("SELECT rm FROM RegressionMetrics rm ORDER BY rm.rSquared DESC")
    List<RegressionMetrics> findTopPerformingModels();
}