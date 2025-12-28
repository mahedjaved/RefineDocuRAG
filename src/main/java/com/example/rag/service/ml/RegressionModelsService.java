package com.example.rag.service.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Adam;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegressionModelsService {

    // DNN params
    private static final int NEURAL_INPUT_SIZE = 17;
    private static final int NEURAL_HIDDEN_SIZE = 32;
    private static final int NEURAL_NUM_EPOCHS = 100;
    private static final double NEURAL_LEARNING_RATE = 0.001;

    private MultiLayerNetwork neuralNetwork;

    /**
     * *****************************
     * LINEAR REGRESSION *
     * *****************************
     */

    public double predictLinearRegression(Map<String, Double> features,
            List<Map<String, Double>> historicalFeatures,
            List<Double> historicalScores) {
        // return a deault 0.5 if no historical data size is less than 2
        try {
            if (historicalFeatures.size() < 2) {
                return 0.5;
            }

            // 1. Prepare training data
            int numFeatures = features.size();
            double[][] X = new double[historicalFeatures.size()][numFeatures];
            double[] y = new double[historicalScores.size()];

            // 2. Loop through historical data and populate X and y
            for (int i = 0; i < historicalFeatures.size(); i++) {
                Map<String, Double> historicalFeature = historicalFeatures.get(i);
                int j = 0;
                for (String key : features.keySet()) {
                    X[i][j++] = historicalFeature.getOrDefault(key, 0.0);
                }
                y[i] = historicalScores.get(i);
            }

            // 3. Train the model
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, X);

            // 4. Obtain predictions
            double[] coefficients = regression.estimateRegressionParameters();
            double prediction = coefficients[0];

            int idx = 1;
            for (String key : features.keySet()) {
                prediction += coefficients[idx++] * features.get(key);
            }

            return Math.max(0.0, Math.min(1.0, prediction));

        } catch (Exception e) {
            log.error("Linear regression error: {}", e.getMessage());
            return 0.5;
        }
    }

    /**
     * *****************************
     * POLYNOMIAL REGRESSION *
     * *****************************
     */

    private double predictPolynomialRegression(Map<String, Double> features,
            List<Map<String, Double>> historicalFeatures,
            List<Double> historicalScores) {
        try {
            if (historicalFeatures.size() < 2) {
                return 0.5;
            }

            // add polynomial features e.g (x^2, x1*x2, etc.)
            Map<String, Double> polyFeatures = addPolynomialFeatures(features);
            List<Map<String, Double>> polyHistorical = new ArrayList<>();
            for (Map<String, Double> hist : historicalFeatures) {
                polyHistorical.add(addPolynomialFeatures(hist));
            }

            int numFeatures = polyFeatures.size();
            double[][] X = new double[polyHistorical.size()][numFeatures];
            double[] y = new double[historicalScores.size()];

            for (int i = 0; i < polyHistorical.size(); i++) {
                Map<String, Double> hist = polyHistorical.get(i);
                int j = 0;
                for (String key : polyFeatures.keySet()) {
                    X[i][j++] = hist.getOrDefault(key, 0.0);
                }
                y[i] = historicalScores.get(i);
            }

            // predict from the regression model
            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(y, X);

            double[] coefficients = regression.estimateRegressionParameters();
            double prediction = coefficients[0];

            int idx = 1;
            for (String key : polyFeatures.keySet()) {
                prediction += coefficients[idx++] * polyFeatures.get(key);
            }
            return Math.max(0.0, Math.min(1.0, prediction));

        } catch (Exception e) {
            log.error("Polynomial regression error: {}", e.getMessage());
            return 0.5;
        }
    }

    private Map<String, Double> addPolynomialFeatures(Map<String, Double> features) {
        Map<String, Double> polyFeatures = new HashMap<>(features);

        // add squared terms
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            String key = entry.getKey() + "_squared";
            polyFeatures.put(key, entry.getValue() * entry.getValue());
        }

        // add interaction terms -- the limits there to prevent gradient explosion
        // TODO: this needs to be optimised to O(n) at least
        List<String> keys = new ArrayList<>(features.keySet());
        for (int i = 0; i < Math.min(5, keys.size()); i++) {
            for (int j = i + 1; j < Math.min(5, keys.size()); j++) {
                String key = keys.get(i) + "x" + keys.get(j);
                polyFeatures.put(key, features.get(keys.get(i)) * features.get(keys.get(j)));
            }
        }
        return polyFeatures;
    }

    /**
     * **********************
     * NEURAL NETWORK *
     * **********************
     */
    public double predictDNN(Map<String, Double> features,
            List<Map<String, Double>> historicalFeatures,
            List<Double> historicalScores) {

        try {
            if (historicalFeatures.size() < 2) {
                return 0.5;
            }

            // Initialize or retrain network
            if (neuralNetwork == null) {
                neuralNetwork = createNeuralNetwork();
            }

            // Train network
            trainNeuralNetwork(historicalFeatures, historicalScores);

            // Predict
            INDArray input = featuresToINDArray(features);
            INDArray output = neuralNetwork.output(input);
            double prediction = output.getDouble(0);

            return Math.max(0.0, Math.min(1.0, prediction));

        } catch (Exception e) {
            log.error("DNN error: {}", e.getMessage());
            return 0.5;
        }
    }

    private MultiLayerNetwork createNeuralNetwork() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(NEURAL_LEARNING_RATE))
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(NEURAL_INPUT_SIZE)
                        .nOut(NEURAL_HIDDEN_SIZE)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(NEURAL_HIDDEN_SIZE)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(16)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .build())
                .build();
        MultiLayerNetwork neuralNetwork = new MultiLayerNetwork(config);
        neuralNetwork.init();
        return neuralNetwork;
    }

    private void trainNeuralNetwork(List<Map<String, Double>> historicalFeatures, List<Double> historicalScores) {
        int numSamples = historicalFeatures.size();
        INDArray input = Nd4j.create(numSamples, NEURAL_INPUT_SIZE);
        INDArray output = Nd4j.create(numSamples, 1);

        for (int i = 0; i < numSamples; i++) {
            INDArray row = featuresToINDArray(historicalFeatures.get(i));
            input.putRow(i, row);
            output.putScalar(new int[] { i, 0 }, historicalScores.get(i));
        }

        DataSet dataset = new DataSet(input, output);
        // train until we hit the final epoch
        for (int i = 0; i < NEURAL_NUM_EPOCHS; i++) {
            neuralNetwork.fit(dataset);
        }
    }

    private INDArray featuresToINDArray(Map<String, Double> features) {
        double[] array = new double[NEURAL_INPUT_SIZE];
        String[] featureNames = {
                "wordCount", "sentenceCount", "avgWordLength", "lexicalDiversity", "punctuationRatio",
                "semanticClarity", "contextRelevance", "specificityScore", "ambiguityScore",
                "hasContext", "hasConstraints", "hasExamples", "structuralComplexity",
                "hasVerbs", "hasNouns", "hasAdjectives", "completenessScore"
        };

        for (int i = 0; i < featureNames.length && i < NEURAL_INPUT_SIZE; i++) {
            array[i] = features.getOrDefault(featureNames[i], 0.0);
        }
        return Nd4j.create(array);
    }

    /**
     * **********************
     * ENSEMBLES PREDICTION *
     * **********************
     */

    public double predictEnsemble(Map<String, Double> features,
            List<Map<String, Double>> historicalFeatures,
            List<Double> historicalScores) {
        try {
            List<Double> predictions = new ArrayList<>();
            List<Double> weights = new ArrayList<>();

            // Linear Regression Method
            try {
                double linearPred = predictLinearRegression(features, historicalFeatures, historicalScores);
                predictions.add(linearPred);
                weights.add(0.3);

            } catch (Exception e) {
                log.error("Error runing linear regression : {}", e.getMessage());
            }

            // Polynomial Regression Method
            try {
                double polyPred = predictPolynomialRegression(features, historicalFeatures, historicalScores);
                predictions.add(polyPred);
                weights.add(0.3);

            } catch (Exception e) {
                log.error("Error runing polynomial regression in ensemble : {}", e.getMessage());
            }

            // Neural Network Method
            try {
                double neuralPred = predictDNN(features, historicalFeatures, historicalScores);
                predictions.add(neuralPred);
                weights.add(0.4);

            } catch (Exception e) {
                log.error("Error runing dnn in ensemble : {}", e.getMessage());
            }

            if (predictions.isEmpty()) {
                return 0.5;
            }

            // Weighted average of the predictions
            double weightedSum = 0.0;
            double totalWeight = 0.0;
            for (int i = 0; i < predictions.size(); i++) {
                weightedSum += predictions.get(i) * weights.get(i);
                totalWeight += weights.get(i);
            }

            return weightedSum / totalWeight;

        } catch (Exception e) {
            log.error("Ensemble error: {}", e.getMessage());
            return 0.5;
        }
    }

    public Map<String, Double> calculateMetrics(List<Double> actual, List<Double> predicted) {
        Map<String, Double> metrics = new HashMap<>();

        if (actual.size() != predicted.size() || actual.isEmpty()) {
            return metrics;
        }

        // MSE
        double mse = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            double error = actual.get(i) - predicted.get(i);
            mse += error * error;
        }
        mse /= actual.size();
        metrics.put("mse", mse);

        // RMSE
        metrics.put("rmse", Math.sqrt(mse));

        // MAE
        double mae = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            mae += Math.abs(actual.get(i) - predicted.get(i));
        }
        mae /= actual.size();
        metrics.put("mae", mae);

        // R-squared
        double meanActual = actual.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double ssTot = 0.0;
        double ssRes = 0.0;
        for (int i = 0; i < actual.size(); i++) {
            ssTot += Math.pow(actual.get(i) - meanActual, 2);
            ssRes += Math.pow(actual.get(i) - predicted.get(i), 2);
        }
        double rSquared = ssTot > 0 ? 1 - (ssRes / ssTot) : 0.0;
        metrics.put("rSquared", rSquared);

        return metrics;
    }
}