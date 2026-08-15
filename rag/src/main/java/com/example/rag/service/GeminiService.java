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
public class GeminiService {

        private final RestClient restClient;
        private final ObjectMapper objectMapper;
        private final String chatUrl;

        public GeminiService(@Value("${gemini.api-key}") String apiKey,
                        ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
                this.chatUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key="
                                + apiKey;
                this.restClient = RestClient.builder()
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();
        }

        public String chat(String prompt) {
                try {
                        Map<String, Object> request = Map.of(
                                        "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
                        String body = objectMapper.writeValueAsString(request);
                        System.out.println("Sending request to Gemini: " + body);
                        String response = restClient.post()
                                        .uri(chatUrl)
                                        .body(body)
                                        .retrieve()
                                        .body(String.class);
                        System.out.println("Response: " + response);
                        JsonNode root = objectMapper.readTree(response);
                        return root.path("candidates").get(0)
                                        .path("content").path("parts").get(0)
                                        .path("text").asText();
                } catch (Exception e) {
                        
                        e.printStackTrace();
                        throw new RuntimeException("Ошибка запроса к Gemini Chat: " + e.getMessage(), e);
                }
        }
}