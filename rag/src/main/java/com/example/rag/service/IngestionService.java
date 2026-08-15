package com.example.rag.service;

import com.example.rag.model.Chunk;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private final CohereEmbeddingService embeddingService;
    private final List<Chunk> chunks = new ArrayList<>();

    @Value("classpath:/docs/sample.pdf")
    private Resource pdfResource;

    public IngestionService(CohereEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostConstruct
    public void loadDocuments() {
        try (InputStream is = pdfResource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            PDDocument document = Loader.loadPDF(bytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);
            document.close();

            String[] paragraphs = fullText.split("\\n\\s*\\n");
            for (String paragraph : paragraphs) {
                if (paragraph.trim().isEmpty())
                    continue;
                float[] embedding = embeddingService.embed(paragraph);
                chunks.add(new Chunk(paragraph, embedding));
            }

            System.out.println("Загружено " + chunks.size() + " фрагментов.");

        } catch (Exception e) {
            System.err.println("Ошибка загрузки PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Chunk> getChunks() {
        return chunks;
    }
}