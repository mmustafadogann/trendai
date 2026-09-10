package com.trendai.trendai.ai.service;

import com.trendai.trendai.ai.tool.CartTool;
import com.trendai.trendai.ai.tool.dto.CartItemToolResult;
import com.trendai.trendai.ai.tool.dto.CartToolResult;
import com.trendai.trendai.entity.CartStatus;
import com.trendai.trendai.repository.CartItemRepository;
import com.trendai.trendai.repository.CartRepository;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingAssistantCartToolCallingTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Test
    void shouldCallGetActiveCartTool() {

        Long userId = 1L;
        Long cartId = 10L;

        com.trendai.trendai.entity.User user =
                new com.trendai.trendai.entity.User();
        user.setId(userId);

        com.trendai.trendai.entity.Cart cart =
                new com.trendai.trendai.entity.Cart();
        cart.setId(cartId);
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);

        com.trendai.trendai.entity.Product product =
                new com.trendai.trendai.entity.Product();
        product.setId(100L);
        product.setName("Laptop");

        com.trendai.trendai.entity.CartItem cartItem =
                new com.trendai.trendai.entity.CartItem();
        cartItem.setId(1000L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("500.00"));

        when(cartRepository.findByUserIdAndStatus(
                userId,
                CartStatus.ACTIVE
        )).thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartId(cartId))
                .thenReturn(List.of(cartItem));

        ChatResponse firstResponse = ChatResponse.builder()
                .aiMessage(
                        AiMessage.builder()
                                .toolExecutionRequests(
                                        List.of(
                                                dev.langchain4j.agent.tool.ToolExecutionRequest
                                                        .builder()
                                                        .id("tool-call-1")
                                                        .name("getActiveCart")
                                                        .arguments("{\"userId\":1}")
                                                        .build()
                                        )
                                )
                                .build()
                )
                .metadata(
                        ChatResponseMetadata.builder()
                                .tokenUsage(new TokenUsage(10, 10))
                                .build()
                )
                .build();

        ChatResponse secondResponse = ChatResponse.builder()
                .aiMessage(
                        AiMessage.builder()
                                .text("Sepetinizde 2 adet Laptop bulunuyor. Toplam 1000 TL.")
                                .build()
                )
                .metadata(
                        ChatResponseMetadata.builder()
                                .tokenUsage(new TokenUsage(10, 10))
                                .build()
                )
                .build();

        when(chatModel.chat(any(ChatRequest.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        CartTool cartTool =
                new CartTool(
                        cartRepository,
                        cartItemRepository
                );

        ShoppingAssistant assistant =
                dev.langchain4j.service.AiServices
                        .builder(ShoppingAssistant.class)
                        .chatModel(chatModel)
                        .tools(cartTool)
                        .build();

        String result = assistant.chat(
                "Sepetimde ne var?"
        );

        assertEquals(
                "Sepetinizde 2 adet Laptop bulunuyor. Toplam 1000 TL.",
                result
        );

        verify(cartRepository)
                .findByUserIdAndStatus(
                        userId,
                        CartStatus.ACTIVE
                );

        verify(cartItemRepository)
                .findByCartId(cartId);

        verify(chatModel, times(2))
                .chat(any(ChatRequest.class));
    }
}