package com.example.rag.controller;

import com.example.rag.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> ask(@RequestBody Map<String, String> request) {
        return Map.of("answer", chatService.ask(request.get("question")));
    }

    @GetMapping
    public Map<String, String> askGet(@RequestParam String q) {
        return Map.of("answer", chatService.ask(q));
    }
}