package com.trendai.trendai.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    @Mock
    private ShoppingAssistant shoppingAssistant;

    @Test
    void shouldReturnAssistantResponse() {

        when(shoppingAssistant.chat("Merhaba"))
                .thenReturn("Merhaba! Size nasıl yardımcı olabilirim?");

        AiAssistantService service =
                new AiAssistantService(shoppingAssistant);

        String result = service.chat("Merhaba");

        assertEquals(
                "Merhaba! Size nasıl yardımcı olabilirim?",
                result
        );

        verify(shoppingAssistant).chat("Merhaba");
    }

    @Test
    void shouldConvertProviderExceptionToAiServiceException() {

        when(shoppingAssistant.chat("Merhaba"))
                .thenThrow(new RuntimeException("Provider error"));

        AiAssistantService service =
                new AiAssistantService(shoppingAssistant);

        AiServiceException exception = assertThrows(
                AiServiceException.class,
                () -> service.chat("Merhaba")
        );

        assertEquals(
                "AI servisine şu anda ulaşılamıyor.",
                exception.getMessage()
        );

        verify(shoppingAssistant).chat("Merhaba");
    }
}