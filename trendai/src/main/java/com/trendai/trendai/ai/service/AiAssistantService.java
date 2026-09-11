package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.ProductDetailsTool;
import com.trendai.trendai.ai.tool.ProductSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.ai.enabled",
        havingValue = "true"
)
public class AiAssistantService {

    private final ShoppingAssistant shoppingAssistant;
    private final AiDemoService aiDemoService;
    private final boolean demoMode;

    @Autowired
    public AiAssistantService(
            ChatModel chatModel,
            ProductSearchTool productSearchTool,
            ProductDetailsTool productDetailsTool,
            AiDemoService aiDemoService,
            @Value("${app.ai.demo-mode:false}") boolean demoMode) {

        this.shoppingAssistant =
                AiServices.builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(
                                productSearchTool,
                                productDetailsTool
                        )
                        .build();

        this.aiDemoService = aiDemoService;
        this.demoMode = demoMode;
    }

    public AiAssistantService(ShoppingAssistant shoppingAssistant) {
        this.shoppingAssistant = shoppingAssistant;
        this.aiDemoService = null;
        this.demoMode = false;
    }

    public String chat(String message) {

        if (demoMode) {
            return aiDemoService.chat(message);
        }

        try {
            return shoppingAssistant.chat(message);
        } catch (Exception e) {
            throw new AiServiceException(
                    "AI servisine şu anda ulaşılamıyor.",
                    e
            );
        }
    }

    public String getMode() {
        return demoMode ? "DEMO" : "LLM";
    }
}