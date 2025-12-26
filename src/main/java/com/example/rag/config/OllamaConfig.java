package com.example.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "ollama")
@Data
public class OllamaConfig {
    private String baseUrl;
    private String model;
    private String embeddingModel;
    private long timeout;
}
