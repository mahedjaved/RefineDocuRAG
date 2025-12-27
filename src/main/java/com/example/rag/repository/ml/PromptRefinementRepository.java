package com.example.rag.repository.ml;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.rag.model.ml.PromptRefinement;

import java.util.List;

@Repository
interface PromptRefinementRepository extends JpaRepository<PromptRefinement, Long> {
    List<PromptRefinement> findBySessionId(String sessionId);
    List<PromptRefinement> findBySessionIdOrderByIterationNumberAsc(String sessionId);

    @Query("SELECT pr FROM PromptRefinement pr WHERE pr.converged = true ORDER BY pr.qualityScore DESC")
    List<PromptRefinement> findConvergedRefinements();

    @Query("SELECT pr FROM PromptRefinement pr WHERE pr.qualityScore >= :minScore ORDER BY pr.qualityScore DESC")
    List<PromptRefinement> findHighQualityRefinements(@Param("minScore") Double minScore);

    @Query("SELECT pr FROM PromptRefinement pr WHERE pr.regressionMethod = :method ORDER BY pr.createdAt DESC")
    List<PromptRefinement> findByRegressionMethod(@Param("method") String method);

    // TODO: investigate if Double is better alt.
    @Query("SELECT AVG(pr.qualityScore) FROM PromptRefinement pr WHERE pr.regressionMethod = :method")
    Integer getAverageScoreByMethod(@Param("method") String method);

    @Query("SELECT pr FROM PromptRefinement pr WHERE pr.iteratioNumber = (SELECT MAX(pr2.iterationNumber) FROM PromptRefinement pr2 WHERE pr2.sessionId = pr.sessionId)")
    List<PromptRefinement> findFinalIterations();

    void deleteBySessionId(String sessionId);
}
