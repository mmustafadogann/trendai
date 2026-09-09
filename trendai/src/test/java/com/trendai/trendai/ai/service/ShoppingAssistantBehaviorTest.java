package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.ProductSearchTool;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.repository.CategoryRepository;
import com.trendai.trendai.service.ProductService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingAssistantBehaviorTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldReturnNoProductMessageWhenSearchResultIsEmpty() {

        ProductPageResponse pageResponse = new ProductPageResponse();
        pageResponse.setContent(List.of());

        when(productService.searchProducts(
                0,
                5,
                "olmayanmarka",
                null,
                "OlmayanMarka",
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        ToolExecutionRequest toolRequest =
                ToolExecutionRequest.builder()
                        .id("tool-call-1")
                        .name("searchProducts")
                        .arguments("""
                                {
                                  "keyword": "olmayanmarka",
                                  "category": null,
                                  "brand": "OlmayanMarka",
                                  "minPrice": null,
                                  "maxPrice": null,
                                  "sort": "id,asc",
                                  "limit": 5
                                }
                                """)
                        .build();

        ChatResponse firstResponse = createChatResponse(
                AiMessage.from(List.of(toolRequest))
        );

        ChatResponse finalResponse = createChatResponse(
                AiMessage.from("Uygun bir ürün bulamadım.")
        );

        when(chatModel.chat(any(ChatRequest.class)))
                .thenReturn(firstResponse)
                .thenReturn(finalResponse);

        ProductSearchTool productSearchTool =
                new ProductSearchTool(
                        productService,
                        categoryRepository
                );

        ShoppingAssistant assistant =
                AiServices.builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(productSearchTool)
                        .build();

        String result = assistant.chat(
                "OlmayanMarka ürünlerini getir."
        );

        assertEquals(
                "Uygun bir ürün bulamadım.",
                result
        );

        verify(productService).searchProducts(
                0,
                5,
                "olmayanmarka",
                null,
                "OlmayanMarka",
                null,
                null,
                "id,asc"
        );
    }

    @Test
    void shouldUseToolForProductQuestion() {

        ProductPageResponse pageResponse = new ProductPageResponse();
        pageResponse.setContent(List.of());

        when(productService.searchProducts(
                0,
                5,
                "telefon",
                null,
                null,
                null,
                null,
                "id,asc"
        )).thenReturn(pageResponse);

        ToolExecutionRequest toolRequest =
                ToolExecutionRequest.builder()
                        .id("tool-call-2")
                        .name("searchProducts")
                        .arguments("""
                                {
                                  "keyword": "telefon",
                                  "category": null,
                                  "brand": null,
                                  "minPrice": null,
                                  "maxPrice": null,
                                  "sort": "id,asc",
                                  "limit": 5
                                }
                                """)
                        .build();

        ChatResponse firstResponse = createChatResponse(
                AiMessage.from(List.of(toolRequest))
        );

        ChatResponse finalResponse = createChatResponse(
                AiMessage.from("Uygun bir ürün bulamadım.")
        );

        when(chatModel.chat(any(ChatRequest.class)))
                .thenReturn(firstResponse)
                .thenReturn(finalResponse);

        ProductSearchTool productSearchTool =
                new ProductSearchTool(
                        productService,
                        categoryRepository
                );

        ShoppingAssistant assistant =
                AiServices.builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(productSearchTool)
                        .build();

        String result = assistant.chat(
                "Telefon bulur musun?"
        );

        assertEquals(
                "Uygun bir ürün bulamadım.",
                result
        );

        verify(productService).searchProducts(
                0,
                5,
                "telefon",
                null,
                null,
                null,
                null,
                "id,asc"
        );
    }

    private ChatResponse createChatResponse(AiMessage aiMessage) {

        ChatResponseMetadata metadata =
                ChatResponseMetadata.builder()
                        .tokenUsage(new TokenUsage(10, 10))
                        .build();

        return ChatResponse.builder()
                .aiMessage(aiMessage)
                .metadata(metadata)
                .build();
    }
}