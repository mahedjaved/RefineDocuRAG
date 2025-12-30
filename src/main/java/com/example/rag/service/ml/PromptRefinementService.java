package com.example.rag.service.ml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rag.dto.nestedDtos.IterationDetail;
import com.example.rag.dto.requestDtos.RefinementRequest;
import com.example.rag.dto.responseDtos.RefinementResponse;
import com.example.rag.dto.responseDtos.RegressionResult;
import com.example.rag.model.ml.FeatureWeight;
import com.example.rag.model.ml.PromptRefinement;
import com.example.rag.model.ml.RegressionMetrics;
import com.example.rag.repository.ml.FeatureWeightRepository;
import com.example.rag.repository.ml.PromptRefinementRepository;
import com.example.rag.repository.ml.RegressionMetricsRepository;
import com.example.rag.service.rag.OllamaService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PromptRefinementService {

    @Autowired
    private FeatureExtractionService featureExtractionService;

    @Autowired
    private RegressionModelsService regressionModelsService;

    @Autowired
    private PromptRefinementRepository refinementRepository;

    @Autowired
    private RegressionMetricsRepository metricsRepository;

    @Autowired
    private FeatureWeightRepository featureWeightRepository;

    @Autowired
    private OllamaService ollamaService;

    private static final double LEARNING_RATE = 0.01;

    @Transactional
    public RefinementResponse refinePrompt(RefinementRequest request) throws Exception {
        String sessionId = UUID.randomUUID().toString();
        log.info("Starting refinement session: {}", sessionId);

        // get prompt and iteration details
        String currentPrompt = request.getPrompt();
        List<IterationDetail> iterations = new ArrayList<>();

        // get feature weights and historical data
        Map<String, Double> featureWeights = initializeFeatureWeights(request.getFeatureWeights());
        List<PromptRefinement> historicalData = refinementRepository.findAll();

        // init scoring params
        double previousScore = 0.0;
        boolean converged = false;
        int iteration = 0;

        for (iteration = 0; iteration < request.getMaxIterations(); iteration++) {
            log.info("Iteration {}: {}", iteration + 1, request.getMaxIterations());

            // extract features from the given prompt
            Map<String, Double> features = featureExtractionService.extractFeatures(currentPrompt);

            // compute the relevant scores
            double qualityScore = featureExtractionService.calculateQualityScore(features, featureWeights);
            double predictedScore = predictScore(request.getRegressionMethod(), features, historicalData);
            String feedback = generateFeedback(features, featureWeights, request.getOptimizationGoals());

            // initi prompt refinement
            PromptRefinement refinement = saveRefinement(sessionId, currentPrompt, iteration, qualityScore,
                    predictedScore, features, feedback, request);

            // add the current iteration detail into the array list using builder pattern
            iterations.add(IterationDetail.builder()
                    .iteration(iteration + 1)
                    .prompt(currentPrompt)
                    .qualityScore(qualityScore)
                    .predictedScore(predictedScore)
                    .feedback(feedback)
                    .build());

            // if quality is exceeds threshold then confirm convergence towards optimal
            // score
            if (qualityScore >= request.getConvergenceThreshold()
                    || (iteration > 0 && Math.abs(qualityScore - previousScore) < 0.01)) {
                converged = true;
                refinement.setConverged(true);
                refinementRepository.save(refinement);
                log.info("Converged at iteration {}", iteration + 1);
                break;
            }

            // update the weights
            updateFeatureWeights(featureWeights, qualityScore, predictedScore, features);

            // if not converged then keep updating the prompt
            if (iteration < request.getMaxIterations()) {
                currentPrompt = refinePromptWithOllama(currentPrompt, feedback, qualityScore);
            }

            previousScore = qualityScore;
        }

        // calculate final metrics for this session
        RegressionResult regressionResult = calculateFinalMetrics(sessionId, request.getRegressionMethod(),
                historicalData);

        // compute the initial, final score and the improvement
        double initialScore = iterations.get(0).getQualityScore();
        double finalScore = iterations.get(iterations.size() - 1).getQualityScore();
        double improvement = ((finalScore - initialScore) / initialScore) * 100;

        return RefinementResponse.builder()
                .sessionId(sessionId)
                .originalPrompt(request.getPrompt())
                .refinedPrompt(currentPrompt)
                .totalIterations(iteration + 1)
                .converged(converged)
                .finalScore(finalScore)
                .initialScore(initialScore)
                .improvementPercentage(improvement)
                .iterations(iterations)
                .RegressionResult(regressionResult)
                .finalFeatures(iterations.get(iterations.size() - 1).getFeatures())
                .regressionMethod(request.getRegressionMethod())
                .build();
    }

    private RegressionResult calculateFinalMetrics(String sessionId, String regressionMethod,
            List<PromptRefinement> historicalData) {

        // if dataset size is too small to perform regression, simply return the status
        // as insufficient to perform any form of prompt refinement
        if (historicalData.size() < 2) {
            return RegressionResult.builder()
                    .method(regressionMethod)
                    .status("Insufficient data")
                    .build();
        }

        // collect all of the actual and predicted scores from historical data
        List<Double> actualScores = historicalData.stream()
                .map(PromptRefinement::getQualityScore)
                .collect(Collectors.toList());

        List<Double> predictedScores = historicalData.stream()
                .map(PromptRefinement::getPredictedScore)
                .collect(Collectors.toList());

        // compute metrics
        Map<String, Double> metrics = regressionModelsService.calculateMetrics(actualScores, predictedScores);

        // update the metrics in DB
        RegressionMetrics metricsEntity = new RegressionMetrics();
        metricsEntity.setSessionId(sessionId);
        metricsEntity.setRegressionMethod(regressionMethod);
        metricsEntity.setMse(metrics.get("mse"));
        metricsEntity.setRmse(metrics.get("rmse"));
        metricsEntity.setMae(metrics.get("mae"));
        metricsEntity.setRSquared(metrics.get("rSquared"));
        metricsEntity.setTrainingDataSize(historicalData.size());
        metricsRepository.save(metricsEntity);

        return RegressionResult.builder()
                .method(regressionMethod)
                .mse(metrics.get("mse"))
                .rmse(metrics.get("rmse"))
                .mae(metrics.get("mae"))
                .rSquared(metrics.get("rSquared"))
                .trainingDataSize(historicalData.size())
                .status("Success")
                .build();

    }

    private String refinePromptWithOllama(String currentPrompt, String feedback, double currentScore) throws Exception {
        String refinementPrompt = String.format(
                "You are a prompt engineering expert. Your task is to improve the following prompt.\n\n" +
                        "Current Prompt:\n%s\n\n" +
                        "Current Quality Score: %.2f\n\n" +
                        "Feedback:\n%s\n\n" +
                        "Please provide an improved version of the prompt that addresses the feedback. " +
                        "Return ONLY the improved prompt, no explanations or preamble.",
                currentPrompt, currentScore, feedback);

        String response = ollamaService.queryWithContext(refinementPrompt, "");

        return response.trim();
    }

    private void updateFeatureWeights(Map<String, Double> featureWeights, double actualScore, double predictedScore,
            Map<String, Double> features) {
        double error = actualScore - predictedScore;

        // loop through the features and perform one set of weight update post grad
        // descent
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String featureName = entry.getKey();
            Double featureValue = entry.getValue();

            // perform weight update - for linear model with a squared loss : wj ← wj-1 −
            // η⋅(error * xj)
            // where wj is newWeight; wj-1 is currentWeight; η is LEARNING_RATE; error is
            // the difference between actualScore and predictedScore; xj is featureValue
            double currentWeight = featureWeights.get(featureName);
            double gradient = error * featureValue;
            double newWeight = currentWeight - LEARNING_RATE * gradient;

            // clip weights between 0 and 1 and add in featureWeights DB
            newWeight = Math.max(0.0, Math.min(1.0, newWeight));
            featureWeights.put(featureName, newWeight);
            updateFeatureWeightInDb(featureName, newWeight);
        }
    }

    private void updateFeatureWeightInDb(String featureName, double newWeight) {
        FeatureWeight fw = featureWeightRepository.findByFeatureName(featureName)
                .orElse(new FeatureWeight());
        fw.setFeatureName(featureName);
        fw.updateWeight(newWeight);
        featureWeightRepository.save(fw);
    }

    private PromptRefinement saveRefinement(String sessionId, String currentPrompt, int iteration, double qualityScore,
            double predictedScore, Map<String, Double> features, String feedback, RefinementRequest request) {
        PromptRefinement refinement = new PromptRefinement();
        refinement.setSessionId(sessionId);
        refinement.setOriginalPrompt(iteration == 0 ? currentPrompt : request.getPrompt());
        refinement.setRefinedPrompt(currentPrompt);
        refinement.setIterationNumber(iteration);
        refinement.setQualityScore(qualityScore);
        refinement.setPredictedScore(predictedScore);
        refinement.setClarityScore(features.get("semanticClarity"));
        refinement.setRelevanceScore(features.get("contextRelevance"));
        refinement.setSpecificityScore(features.get("specificityScore"));
        refinement.setCompletenessScore(features.get("completenessScore"));
        refinement.setRegressionMethod(request.getRegressionMethod());
        refinement.setFeedback(feedback);
        refinement.setExtractedFeatures(features);
        refinement.setMaxIterations(request.getMaxIterations());
        refinement.setConvergenceThreshold(request.getConvergenceThreshold());
        refinement.setOptimizationGoals(request.getOptimizationGoals());

        return refinementRepository.save(refinement);
    }

    private String generateFeedback(Map<String, Double> features, Map<String, Double> weights,
            List<String> optimizationGoals) {
        StringBuilder feedback = new StringBuilder();
        feedback.append("ANALYSIS: \n");

        // sort the features according to their score * weight
        List<Map.Entry<String, Double>> sortedFeatures = features.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> e.getValue() * weights.getOrDefault(e.getKey(), 0.0)))
                .collect(Collectors.toList());

        feedback.append("\nAreas for improvement:\n");

        // Takes up to the first 3 entries from the sorted list (those with the lowest
        // weighted scores, i.e. “worst” features under this scheme).
        // For each of those, appends a bullet line with the feature name and its raw
        // score, formatted to two decimal places.
        // Produces a short list of top problem areas.
        for (int i = 0; i < Math.min(3, sortedFeatures.size()); i++) {
            Map.Entry<String, Double> entry = sortedFeatures.get(i);
            feedback.append(String.format("- %s (score: %.2f)\n",
                    entry.getKey(), entry.getValue()));
        }

        // adds a "Recommendations:" header and appends goal‑specific recommendation
        // text based on the feature data
        // creates tailored guidance aligned with those goals.
        if (optimizationGoals != null) {
            feedback.append("\nRecommendations:\n");
            for (String goal : optimizationGoals) {
                feedback.append(getRecommendation(goal, features));
            }
        }
        return feedback.toString();

    }

    private Object getRecommendation(String goal, Map<String, Double> features) {
        // basic examples of recommendations for each goal
        switch (goal.toUpperCase()) {
            case "CLARITY":
                return "- Improve clarity by using more specific action words and reducing ambiguous terms\n";
            case "RELEVANCE":
                return "- Add relevant context about the domain or use case\n";
            case "COMPLETENESS":
                return "- Ensure the prompt includes all necessary grammatical elements\n";
            case "SPECIFICITY":
                return "- Include specific details, examples, or constraints\n";
            default:
                return "";
        }
    }

    private double predictScore(String regressionMethod, Map<String, Double> features,
            List<PromptRefinement> historicalData) {
        if (historicalData.isEmpty()) {
            return 0.5;
        }

        List<Map<String, Double>> historicalFeatures = historicalData.stream()
                .map(PromptRefinement::getExtractedFeatures)
                .collect(Collectors.toList());

        List<Double> historicalScores = historicalData.stream()
                .map(PromptRefinement::getQualityScore)
                .collect(Collectors.toList());

        switch (regressionMethod.toUpperCase()) {
            case "LINEAR":
                return regressionModelsService.predictLinearRegression(
                        features, historicalFeatures, historicalScores);
            case "POLYNOMIAL":
                return regressionModelsService.predictPolynomialRegression(
                        features, historicalFeatures, historicalScores);
            case "NEURAL":
                return regressionModelsService.predictDNN(
                        features, historicalFeatures, historicalScores);
            case "ENSEMBLE":
            default:
                return regressionModelsService.predictEnsemble(
                        features, historicalFeatures, historicalScores);
        }
    }

    private Map<String, Double> initializeFeatureWeights(Map<String, Double> featureWeights) {
        Map<String, Double> weights = new HashMap<>();

        weights.put("semanticClarity", 0.15);
        weights.put("contextRelevance", 0.15);
        weights.put("specificityScore", 0.12);
        weights.put("ambiguityScore", 0.10);
        weights.put("lexicalDiversity", 0.08);
        weights.put("completenessScore", 0.08);
        weights.put("hasContext", 0.07);
        weights.put("hasConstraints", 0.06);
        weights.put("structuralComplexity", 0.05);
        weights.put("wordCount", 0.04);
        weights.put("sentenceCount", 0.03);
        weights.put("avgWordLength", 0.02);
        weights.put("punctuationRatio", 0.02);
        weights.put("hasExamples", 0.01);
        weights.put("hasVerbs", 0.01);
        weights.put("hasNouns", 0.01);
        weights.put("hasAdjectives", 0.01);

        List<FeatureWeight> dbWeights = featureWeightRepository.findAll();
        for (FeatureWeight fw : dbWeights) {
            weights.put(fw.getFeatureName(), fw.getWeight());
        }

        if (featureWeights != null) {
            weights.putAll(featureWeights);
        }

        return weights;
    }

}
