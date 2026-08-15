package com.example.rag.model;

public record Chunk(String text, float[] embedding) {
}