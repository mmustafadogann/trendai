package com.trendai.trendai.ai.service;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private ChatModel chatModel;

    @Test
    void shouldReturnModelResponse() {
        when(chatModel.chat("Merhaba"))
                .thenReturn("Merhaba! Size nasıl yardımcı olabilirim?");

        AiAssistantService service = new AiAssistantService(chatModel);

        String result = service.chat("Merhaba");

        assertEquals(
                "Merhaba! Size nasıl yardımcı olabilirim?",
                result
        );

        verify(chatModel).chat("Merhaba");
    }

    @Test
    void shouldConvertProviderExceptionToAiServiceException() {
        when(chatModel.chat("Merhaba"))
                .thenThrow(new RuntimeException("Provider error"));

        AiAssistantService service = new AiAssistantService(chatModel);

        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> service.chat("Merhaba")
        );

        assertEquals(
                "AI servisine şu anda ulaşılamıyor.",
                exception.getMessage()
        );

        verify(chatModel).chat("Merhaba");
    }
}