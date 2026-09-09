package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.ProductSearchTool;
import com.trendai.trendai.dto.ProductPageResponse;
import com.trendai.trendai.dto.ProductResponse;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingAssistantToolCallingTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void shouldCallProductSearchToolForProductQuestion() {

        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setBrand("Apple");
        product.setPrice(new BigDecimal("20000"));
        product.setStock(5);
        product.setCategoryName("Elektronik");

        ProductPageResponse pageResponse = new ProductPageResponse();
        pageResponse.setContent(List.of(product));

        when(productService.searchProducts(
                0,
                3,
                "iphone",
                null,
                null,
                null,
                null,
                "price,asc"
        )).thenReturn(pageResponse);

        ToolExecutionRequest toolRequest =
                ToolExecutionRequest.builder()
                        .id("tool-call-1")
                        .name("searchProducts")
                        .arguments("""
                                {
                                  "keyword": "iphone",
                                  "category": null,
                                  "brand": null,
                                  "minPrice": null,
                                  "maxPrice": null,
                                  "sort": "price,asc",
                                  "limit": 3
                                }
                                """)
                        .build();

        ChatResponse firstResponse = createChatResponse(
                AiMessage.from(List.of(toolRequest))
        );

        ChatResponse finalResponse = createChatResponse(
                AiMessage.from(
                        "iPhone 15 — 20.000 TL. Elektronik kategorisinde ve stokta mevcut."
                )
        );

        when(chatModel.chat(any(ChatRequest.class)))
                .thenReturn(firstResponse)
                .thenReturn(finalResponse);

        ProductSearchTool productSearchTool =
                new ProductSearchTool(
                        productService,
                        categoryRepository
                );

        ShoppingAssistant shoppingAssistant =
                AiServices.builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(productSearchTool)
                        .build();

        String result =
                shoppingAssistant.chat(
                        "20.000 TL civarında iPhone bulur musun?"
                );

        assertEquals(
                "iPhone 15 — 20.000 TL. Elektronik kategorisinde ve stokta mevcut.",
                result
        );

        verify(productService).searchProducts(
                0,
                3,
                "iphone",
                null,
                null,
                null,
                null,
                "price,asc"
        );
    }

    private ChatResponse createChatResponse(AiMessage aiMessage) {

        ChatResponseMetadata metadata =
                ChatResponseMetadata.builder()
                        .tokenUsage(
                                new TokenUsage(10, 10)
                        )
                        .build();

        return ChatResponse.builder()
                .aiMessage(aiMessage)
                .metadata(metadata)
                .build();
    }
}