package com.example.rag.service.ml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FeatureExtractionService {

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");

    public Map<String, Double> extractFeatures(String prompt) {
        Map<String, Double> features = new HashMap<>();

        // linguistic features - 1) Word Count 2) Sentence Count 3) Average Word Length
        // 4) Lexical Diversity 5) Punctuation Ratio
        features.put("wordCount", (double) getWordCount(prompt));
        features.put("sentenceCount", (double) getSentenceCount(prompt));
        features.put("averageWordLength", getAverageWordLength(prompt));
        features.put("lexicalDiversity", getLexicalDiversity(prompt));
        features.put("punctuationRatio", getPunctuationRatio(prompt));

        // semantic features - 1) Semantic Clarity 2) Context Relevance 3) Specificity
        // 4) Ambiguity
        features.put("semanticClarity", calculateSemanticClarity(prompt));
        features.put("contextRelevance", calculateRelevance(prompt));
        features.put("specificity", calculateSemanticClarity(prompt));
        features.put("ambiguity", calculateSemanticClarity(prompt));

        // structural features - 1) Does prompt have context, constraints, examples 2)
        // Calculate the structural complexity
        features.put("hasContext", hasContext(prompt) ? 1.0 : 0.0);
        features.put("hasConstraints", hasConstraints(prompt) ? 1.0 : 0.0);
        features.put("hasExamples", hasExamples(prompt) ? 1.0 : 0.0);
        features.put("structuralComplexity", calculateStructuralComplexity(prompt));

        // completeness features - 1) Prompt has nouns, adjectives, verbs 2) Calculate
        // the completeness score
        features.put("hasNouns", hasNouns(prompt) ? 1.0 : 0.0);
        features.put("hasAdjectives", hasAdjectives(prompt) ? 1.0 : 0.0);
        features.put("hasVerbs", hasVerbs(prompt) ? 1.0 : 0.0);
        features.put("completenessScore", calculateCompleteness(prompt));

        log.debug("Extracted {} features from prompt", features.size());
        return features;
    }

    private double calculateCompleteness(String prompt) {
        // Prompts that dont have verbs, nouns, adjectives and context are not scored
        double score = 0.0;
        if (hasVerbs(prompt))
            score += 0.25;
        if (hasNouns(prompt))
            score += 0.25;
        if (hasAdjectives(prompt))
            score += 0.25;
        if (hasContext(prompt))
            score += 0.25;
        return score;
    }

    private boolean hasVerbs(String prompt) {
        String[] commonVerbs = { "is", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "do", "does", "did",
                "write", "create", "make", "generate", "explain", "describe" };
        for (String verb : commonVerbs) {
            if (prompt.toLowerCase().matches(".*\\b" + verb + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAdjectives(String prompt) {
        String[] commonAdjectives = { "good", "bad", "great", "small", "large", "new", "old",
                "important", "specific", "detailed", "comprehensive" };
        for (String adj : commonAdjectives) {
            if (prompt.toLowerCase().contains(adj)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNouns(String prompt) {
        // search for common nouns
        return prompt.split("\\s+").length > 2;
    }

    private Double calculateStructuralComplexity(String prompt) {
        double score = 0.0;

        // sentence structure
        int sentenceCount = getSentenceCount(prompt);
        score += Math.min(0.3, sentenceCount * 0.1);

        // clause indicators
        String[] clauseMarkers = { ",", ";", ":", "and", "but", "or", "because", "when", "if" };
        for (String marker : clauseMarkers) {
            if (prompt.contains(marker)) {
                score += 0.05;
            }
        }
        return Math.min(1.0, score);
    }

    private boolean hasExamples(String prompt) {
        String[] exampleMarkers = { "for example", "such as", "like", "e.g.", "for instance",
                "including", "namely" };
        for (String marker : exampleMarkers) {
            if (prompt.toLowerCase().contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConstraints(String prompt) {
        String[] constraintMarkers = { "must", "should", "need to", "required", "limit",
                "within", "between", "maximum", "minimum", "at least" };
        for (String marker : constraintMarkers) {
            if (prompt.toLowerCase().contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasContext(String prompt) {
        String[] contextMarkers = { "in", "for", "about", "regarding", "related to",
                "in the context", "background", "scenario" };
        for (String marker : contextMarkers) {
            if (prompt.toLowerCase().contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private Double calculateRelevance(String prompt) {
        double score = 0.3;

        // Context indicators - 15% of the score if any of the indicators are present
        String[] contextIndicators = { "in the context of", "regarding", "about", "for",
                "related to", "concerning", "with respect to" };
        for (String indicator : contextIndicators) {
            if (prompt.toLowerCase().contains(indicator)) {
                score += 0.15;
            }
        }

        // Use of domain specific words - 10% of the score if any of the words are
        // present
        if (prompt.matches(".*\\b(technical|scientific|business|creative|academic)\\b.*")) {
            score += 0.1;
        }

        return Math.min(1.0, score);
    }

    private Double calculateSemanticClarity(String prompt) {
        double score = 0.5;
        // example of using action words to produce outputs with more clarity
        String[] actionWords = { "explain", "describe", "analyze", "create", "write",
                "generate", "summarize", "compare", "list", "provide" };
        for (String word : actionWords) {
            if (prompt.toLowerCase().contains(word)) {
                score += 0.1;
            }
        }
        // vice versa for vague outputs
        String[] vagueWords = { "thing", "stuff", "something", "somehow", "maybe" };
        for (String word : vagueWords) {
            if (prompt.toLowerCase().contains(word)) {
                score -= 0.1;
            }
        }
        return Math.max(0.0, Math.min(1.0, score));
    }

    public double calculateQualityScore(Map<String, Double> features, Map<String, Double> weights) {
        double score = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String featureName = entry.getKey();
            Double featureValue = entry.getValue();
            Double weight = weights.getOrDefault(featureName, 0.05);

            score += featureValue * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? score / totalWeight : 0.0;
    }

    private Double getPunctuationRatio(String prompt) {
        int punctuationCount = 0;
        for (char c : prompt.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) {
                punctuationCount++;
            }
        }
        return prompt.length() > 0 ? (double) punctuationCount / prompt.length() : 0.0;
    }

    private Double getLexicalDiversity(String prompt) {
        String[] words = prompt.toLowerCase().split("\\s+");
        if (words.length == 0)
            return 0.0;

        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        return (double) uniqueWords.size() / words.length;
    }

    private Double getAverageWordLength(String prompt) {
        String[] words = prompt.split("\\s+");
        if (words.length == 0)
            return 0.0;

        double totalLength = 0;
        for (String word : words) {
            totalLength += word.replaceAll("[^a-zA-Z]", "").length();
        }
        return totalLength / words.length;
    }

    private int getSentenceCount(String prompt) {
        String[] sentences = SENTENCE_PATTERN.split(prompt);
        return Math.max(1, sentences.length);
    }

    private double getWordCount(String prompt) {
        return prompt.trim().split("\\s+").length;
    }
}
