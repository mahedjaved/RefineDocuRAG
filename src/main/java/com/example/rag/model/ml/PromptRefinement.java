package com.example.rag.model.ml;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
-- Identifiers and metadata
* id
Primary key for the row; unique identifier for a single prompt refinement step in the database.

* sessionId
Identifier that groups refinements belonging to the same user/session or conversation, so you can query all refinements for one interaction flow.

* createdAt
Timestamp set right before persisting (@PrePersist) that records when this refinement entry was created.

-- Prompt text fields
* originalPrompt
The original prompt text before refinement; stored as a large text field (CLOB) to support arbitrarily long prompts.

* refinedPrompt
The improved/modified version of the prompt produced at this refinement step; also stored as CLOB to handle long text.

* feedback
Free-form text feedback about this refinement, such as human or model comments explaining quality, issues, or suggestions.

-- Iteration and convergence control
* iterationNumber
The iteration index within a session, e.g. 0, 1, 2…, indicating which refinement step this record corresponds to.

* maxIterations
Optional cap on how many refinement steps are allowed for this session/strategy; used to stop the process after a certain number of iterations.

* convergenceThreshold
Optional numeric threshold for deciding convergence (for example, minimum required improvement in quality score between iterations).

* converged
Flag indicating whether the optimization/refinement process has met the convergence criteria at this iteration (true = converged, false = still improving or failed to converge).

-- Scoring and evaluation
* qualityScore
Core numeric evaluation of the refined prompt quality at this step (e.g. human rating or model-computed score).

* predictedScore
Score predicted by a regression/ML model for this prompt (for example, what the model expects the qualityScore to be).

* clarityScore
Numeric score focusing on how clear or unambiguous the refined prompt is.

* relevanceScore
Numeric score measuring how relevant the refined prompt is to the original task or user intent.

* specificityScore
Numeric score for how specific and detailed the prompt is, as opposed to being vague.

* completenessScore
Numeric score measuring how fully the prompt covers all necessary aspects/requirements of the task.

-- Regression / model configuration
* regressionMethod
String describing which regression or modeling approach was used to predict or optimize scores for this refinement step, e.g. "linear", "polynomial", "ensemble", "dnn".

* extractedFeatures
A key–value map persisted in a separate table (refinement_features) where:
 key (feature_name) = name of a feature (e.g. avg_token_length, num_constraints), value (feature_value) = numeric value of that feature for this refinement.
This captures the feature vector used by regression/ML methods.

-- Optimization objectives
* optimizationGoals
A list of textual goals stored in a separate table (optimization_goals), such as "maximize clarity", "increase relevance", "balance length and detail".
This documents what the refinement process is trying to optimize at this step or for this session.
 */
@Entity
@Table(name = "prompt_refinements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromptRefinement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sessionId;

    @Lob
    @Column(columnDefinition = "CLOB", nullable = false)
    private String originalPrompt;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String refinedPrompt;

    @Column(nullable = false)
    private Integer iterationNumber;

    @Column(nullable = false)
    private Double qualityScore;

    private Double predictedScore;

    private Double clarityScore;

    private Double relevanceScore;

    private Double specificityScore;

    private Double completenessScore;

    @Column(nullable = false)
    private String regressionMethod;    // linear, polynomial, ensemble and DNN based

    @Lob
    @Column(columnDefinition = "CLOB")
    private String feedback;

    @ElementCollection
    @CollectionTable(name = "refinement_features", joinColumns = @JoinColumn(name = "refinement_id"))
    @MapKeyColumn(name = "feature_name")
    @Column(name = "feature_value")
    private Map<String, Double> extractedFeatures = new HashMap<>();

    @Column(nullable = false)
    private Boolean converged = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private Integer maxIterations;

    private Double convergenceThreshold;

    @ElementCollection
    @CollectionTable(name = "optimization_goals", joinColumns = @JoinColumn(name = "refinement_id"))
    @Column(name = "goal")
    private List<String> optimizationGoals;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}