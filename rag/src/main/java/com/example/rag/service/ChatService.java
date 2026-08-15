package com.example.rag.service;

import com.example.rag.model.Chunk;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ChatService {

    private final GeminiService geminiService;
    private final CohereEmbeddingService embeddingService;
    private final IngestionService ingestionService;

    public ChatService(GeminiService geminiService,
            CohereEmbeddingService embeddingService,
            IngestionService ingestionService) {
        this.geminiService = geminiService;
        this.embeddingService = embeddingService;
        this.ingestionService = ingestionService;
    }

    public String ask(String question) {
        try {
            List<Chunk> chunks = ingestionService.getChunks();
            if (chunks.isEmpty()) {
                return "Документ не загружен. Проверьте, что PDF файл есть в resources/docs/sample.pdf.";
            }

            float[] questionEmbedding = embeddingService.embed(question);

            List<Chunk> topChunks = chunks.stream()
                    .map(chunk -> new ScoredChunk(chunk, cosineSimilarity(questionEmbedding, chunk.embedding())))
                    .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                    .limit(4)
                    .map(ScoredChunk::chunk)
                    .toList();

            String context = topChunks.stream()
                    .map(Chunk::text)
                    .reduce((a, b) -> a + "\n\n" + b)
                    .orElse("");

            String prompt = """
                    Ты — ассистент. Отвечай на вопрос, используя только контекст.
                    Если ответа нет в контексте, скажи "Я не знаю".

                    Контекст:
                    %s

                    Вопрос: %s
                    Ответ:""".formatted(context, question);

            return geminiService.chat(prompt);

        } catch (Exception e) {
            return "Ошибка при обработке запроса: " + e.getMessage();
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredChunk(Chunk chunk, double score) {
    }
}