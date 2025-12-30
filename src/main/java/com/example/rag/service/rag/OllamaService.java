package com.example.rag.service.rag;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.rag.config.OllamaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
// @RequiredArgsConstructor
@Slf4j
public class OllamaService {
    private final OllamaConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    public OllamaService(OllamaConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
            .build();
    }

    public String generateEmbedding(String text) throws IOException {
        // Create request object
        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("model", config.getEmbeddingModel());
        requestNode.put("prompt", text);
        
        String json = objectMapper.writeValueAsString(requestNode);
        log.info("Request JSON: {}", json);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/api/embeddings")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to generate embedding: " + response);
            }
            return response.body().string();
        }
    }

    public String queryWithContext(String query, String context) throws IOException {
        /**
         * In this example context is simply kept as a string.
         */
        String prompt = String.format(
                "Context: %s\n\nQuestion: %s\n\nAnswer based only on the context above:",
                context, query);

        ObjectNode requestNode = objectMapper.createObjectNode();
        requestNode.put("model", config.getModel());
        requestNode.put("prompt", prompt);
        requestNode.put("stream", false);

        String json = objectMapper.writeValueAsString(requestNode);

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/api/generate")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Ollama request failed: code={}, message={}", response.code(), response.message());
                throw new IOException("Failed to query Ollama: " + response);
            }

            String responseBody = response.body().string();
            log.info("Ollama response: {}", responseBody);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("response")) {
                return jsonNode.get("response").asText();
            } else {
                 log.error("Ollama response missing 'response' field: {}", responseBody);
                 throw new IOException("Ollama response missing 'response' field");
            }
        } catch (Exception e) {
            log.error("Error querying Ollama", e);
            throw e;
        }
    }
}