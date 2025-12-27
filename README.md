# Project Description

## Motivation

* The project is motivated by the need to improve the quality of generated code by leveraging the power of large language models (LLMs).
* In industry applications of RAG based querying system, the quality of the generated answer to query is improved via change of backend model.
* Task switching is expensive if purely relying on model switching.
* This repository aims to provide a solution of rag-refinement using machine learning techniques to improve the quality of generated answers.

## Problem Statement

* The problem statement is to develop a machine learning model that can improve the quality of generated answers to queries in a RAG based querying system.

## Methodology

* A simple implementation of a rag is used in this project.
* Demo repo available at : https://github.com/mahedjaved/ReactRAGpdf
* It is a Java-Spring-React based implementation of a rag based querying system.
* This codebase will be improved step-by-step as the project progresses.

# Record of Improvement

* The project will be improved step-by-step as the project progresses.
* The record of improvement will be documented in this section.
- ☐ Build the advanced regression pipeline 
    - ☐ 4 Regression Methods: Linear, Polynomial, Neural Networks, Ensemble
    - ☐ Gradient Descent Learning: Dynamic weight optimization
    - ☐ Iterative Refinement: Feedback-driven improvement loop
    - ☐ Comprehensive Metrics: MSE, RMSE, MAE, R²
- ☐ Build the domain transfer objects - this is done to transfer the data between the client and the server without impacting the database schema with concrete implementation details. Entities represent database structure (internal) vs DTOs represent API structure (external). Complimenting seperation of concerns.
    - ☑ Request DTOs (Frontend → Backend)
        - ☑ RefinementRequest - Parameters to refine a prompt
        - ☑ ChatRequest - Question for RAG system

    - ☐ Response DTOs (Backend → Frontend)
        - ☐ RefinementResponse - Complete refinement results
        - ☐ ChatResponse - RAG answer with sources
        - ☐ UploadResponse - File upload confirmation
        - ☐ DocumentInfo - Document listing
        - ☐ FeatureWeightInfo - Feature weight display
        - ☐ MetricsSummary - Aggregated metrics by method
        - ☐ SessionSummary - Refinement session summary
        - ☑ RegressionResult - ML model metrics

    - ☐ Nested DTOs (Parts of other DTOs)
        - ☑ IterationDetail - One refinement iteration
        - ☑ SourceReference - RAG source document
        - ☑ FeatureAnalysis - Prompt quality analysis


* The stratergy of implementation is as follows
* Evaluation of prompt will be assessed by : quality, accuracy of predicted , clarity, relevance to subject,  specificity to subject, completeness of the response
* Use regression methods to quantify the impact of each factor on the qualityScore:  linear, polynomial, neural networks, ensemble
* In our example we will assume that refinement features (vectors used by regression/ML methods) will be stored in a separate table (`refinement_features`) where:
    - feature_name = name of a feature (e.g. avg_token_length, num_constraints),
    - feature_value = numeric value of that feature for this refinement.

## Reusable Components for Markdown
- ☐ Task not started
- ☑ Task completed
- ✅ Fully verified