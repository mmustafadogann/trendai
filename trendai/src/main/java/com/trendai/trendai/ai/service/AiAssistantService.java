package com.trendai.trendai.ai.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.ai.enabled",
        havingValue = "true"
)
public class AiAssistantService {

    private final ChatModel chatModel;

    public AiAssistantService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        try {
            return chatModel.chat(message);
        } catch (Exception e) {
            throw new AiServiceException(
                    "AI servisine şu anda ulaşılamıyor.",
                    e
            );
        }
    }
}