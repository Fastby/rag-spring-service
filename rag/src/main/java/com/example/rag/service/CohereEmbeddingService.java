package com.example.rag.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class CohereEmbeddingService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String embeddingUrl = "https://api.cohere.ai/v1/embed";

    public CohereEmbeddingService(@Value("${cohere.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public float[] embed(String text) {
        try {
            Map<String, Object> request = Map.of(
                    "texts", List.of(text),
                    "model", "embed-english-v3.0",
                    "input_type", "search_document");
            String body = objectMapper.writeValueAsString(request);
            String response = restClient.post()
                    .uri(embeddingUrl)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddings = root.path("embeddings").get(0);
            float[] embedding = new float[embeddings.size()];
            for (int i = 0; i < embeddings.size(); i++) {
                embedding[i] = (float) embeddings.get(i).asDouble();
            }
            return embedding;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения эмбеддинга от Cohere: " + e.getMessage(), e);
        }
    }
}