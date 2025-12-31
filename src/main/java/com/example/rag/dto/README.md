- ☑ Build the domain transfer objects - this is done to transfer the data between the client and the server without impacting the database schema with concrete implementation details. Entities represent database structure (internal) vs DTOs represent API structure (external). Complimenting seperation of concerns.
    - ☑ Request DTOs (Frontend → Backend)
        - ✅ RefinementRequest - Parameters to refine a prompt
        - ☑ ChatRequest - Question for RAG system

    - ☑ Response DTOs (Backend → Frontend)
        - ☑ RefinementResponse - Complete refinement results
        - ☑ ChatResponse - RAG answer with sources
        - ☑ UploadResponse - File upload confirmation
        - ☑ DocumentInfo - Document listing
        - ☑ FeatureWeightInfo - Feature weight display
        - ☑ MetricsSummary - Aggregated metrics by method
        - ☑ SessionSummary - Refinement session summary
        - ☑ RegressionResult - ML model metrics

    - ☑ Nested DTOs (Parts of other DTOs)
        - ☑ IterationDetail - One refinement iteration
        - ☑ SourceReference - RAG source document
        - ☑ FeatureAnalysis - Prompt quality analysis
