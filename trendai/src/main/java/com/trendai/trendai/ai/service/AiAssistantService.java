package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.CartTool;
import com.trendai.trendai.ai.tool.ProductSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.ai.enabled",
        havingValue = "true"
)
public class AiAssistantService {

    private final ShoppingAssistant shoppingAssistant;

    public AiAssistantService(
            ChatModel chatModel,
            ProductSearchTool productSearchTool,
            CartTool cartTool) {

        this(
                AiServices.builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(
                                productSearchTool,
                                cartTool
                        )
                        .build()
        );
    }

    AiAssistantService(ShoppingAssistant shoppingAssistant) {
        this.shoppingAssistant = shoppingAssistant;
    }

    public String chat(String message) {
        try {
            return shoppingAssistant.chat(message);
        } catch (Exception e) {
            throw new AiServiceException(
                    "AI servisine şu anda ulaşılamıyor.",
                    e
            );
        }
    }
}