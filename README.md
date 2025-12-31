# RAG Refinement 🚀
> **🚧 Work in Progress (Draft)**: This project is currently in active development. Features and APIs are subject to change.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-7.0-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Ollama](https://img.shields.io/badge/Ollama-Local_LLM-black?style=for-the-badge&logo=ollama&logoColor=white)

## 📖 Overview

**RAG Refinement** is a full-stack proof-of-concept application designed to enhance the quality of Retrieval-Augmented Generation (RAG) systems. Instead of relying solely on switching backend LLMs to improve answer quality—which can be computationally expensive and operationally complex—this project introduces an **ML-driven Prompt Refinement** layer.

By analyzing prompt characteristics (clarity, specificity, relevance) and applying regression models (Linear, Polynomial, Ensemble, Neural), the system iteratively optimizes user queries *before* they are sent to the LLM, resulting in higher-quality generated responses.

### 🎯 Problem Statement
In industrial RAG applications, improving query answer quality often requires upgrading to larger, slower, or more expensive models. This project aims to solve this by:
*   Developing a lightweight machine learning model to score and refine prompts.
*   Reducing the dependency on "smart" models by making the input "smarter".
*   Providing a feedback loop for continuous prompt improvement.

---

## ✨ Key Features

*   **📄 PDF Document RAG**: Upload and chat with PDF documents using local embeddings.
*   **🤖 ML-Powered Refinement**: Automatically rewrite vague prompts using Ensemble, Linear, or Neural regression methods.
*   **📊 Refinement Metrics**: Real-time feedback on prompt improvement (e.g., "+15% Clarity").
*   **💬 Interactive Chat UI**: Modern, responsive interface built with React and Tailwind CSS.
*   **🔌 Local LLM Integration**: Fully privacy-focused using **Ollama** (Mistral 7B & Nomic Embed Text).

---

## 🛠️ Tech Stack

### Backend
*   **Core**: Java 17, Spring Boot 3.2.4
*   **AI/ML**: DeepLearning4j (DL4J), LangChain4j
*   **Database**: H2 (In-Memory)
*   **Build Tool**: Maven

### Frontend
*   **Framework**: React 19
*   **Build Tool**: Vite 7
*   **Styling**: Tailwind CSS 4, Lucide React (Icons)

### Infrastructure
*   **LLM Serving**: Ollama (running locally)

---

## 🚀 Getting Started

### Prerequisites
1.  **Java Development Kit (JDK) 17** or higher.
2.  **Node.js** (v18+ recommended) and **npm**.
3.  **Ollama**: Installed and running.
    *   Pull required models:
        ```bash
        ollama pull mistral
        ollama pull nomic-embed-text
        ```

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/mahedjaved/ReactRAGpdf.git
cd rag-refinement
```

#### 2. Backend Setup
Navigate to the project root and run the Spring Boot application:
```bash
# Windows
mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```
*The backend will start on `http://localhost:8080`.*

#### 3. Frontend Setup
Open a new terminal, navigate to the frontend directory, and start the development server:
```bash
cd src/main/rag-frontend
npm install
npm run dev
```
*The frontend will start on `http://localhost:5173`.*

---

## 🧩 Methodology & Architecture

The system follows a **Refine-Then-Retrieve** pattern:
1.  **Input**: User enters a raw prompt (e.g., "code for login").
2.  **Feature Extraction**: The system analyzes the prompt for features like `avg_token_length`, `specificity_score`, and `ambiguity`.
3.  **Regression**: An ML model (Ensemble/Linear/Neural) predicts a `quality_score`.
4.  **Optimization**: If the score is below threshold, the prompt is rewritten to maximize these feature weights.
5.  **Retrieval**: The optimized prompt is used to query the vector database (PDF chunks).
6.  **Generation**: The LLM generates a final answer based on the refined context.

**Note on Dependencies**:
To ensure lightweight performance, the core `deeplearning4j` library was optimized to use the `nn` module, excluding unnecessary heavy dependencies like `slf4j-api` to prevent classpath conflicts.

```xml
<dependency>
    <groupId>org.deeplearning4j</groupId>
    <artifactId>deeplearning4j-nn</artifactId>
    <version>${dl4j.version}</version>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 🗺️ Roadmap & Status

This project is being improved step-by-step. Below is the current record of improvement:

*   **Prompt Evaluation Criteria**: Quality, Accuracy, Clarity, Relevance, Specificity, Completeness.
*   **Refinement Strategy**: Storing refinement features in a dedicated `refinement_features` table for historical analysis.

### Development Checklist
- [ ] **Build Advanced Regression Pipeline**
    - [ ] 4 Regression Methods: Linear, Polynomial, Neural Networks, Ensemble
    - [ ] Gradient Descent Learning: Dynamic weight optimization
    - [ ] Iterative Refinement: Feedback-driven improvement loop
    - [ ] Comprehensive Metrics: MSE, RMSE, MAE, R²
- [x] **Basic RAG Implementation** (Java-Spring-React)
- [x] **Frontend Refinement UI** (Integration complete)

---

## 🤝 Contributing

Contributions are welcome! Since this is a draft project, please open an issue first to discuss what you would like to change.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
