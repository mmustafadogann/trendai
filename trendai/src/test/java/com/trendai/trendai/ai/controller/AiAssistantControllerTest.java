package com.trendai.trendai.ai.controller;

import com.trendai.trendai.ai.service.AiAssistantService;
import com.trendai.trendai.ai.service.AiServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiAssistantController.class)
@TestPropertySource(properties = "app.ai.enabled=true")
class AiAssistantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiAssistantService aiAssistantService;

    @Test
    void shouldReturnAiResponse() throws Exception {
        when(aiAssistantService.chat("Merhaba"))
                .thenReturn("Merhaba! Size nasıl yardımcı olabilirim?");

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Merhaba"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.answer")
                        .value("Merhaba! Size nasıl yardımcı olabilirim?"));

        verify(aiAssistantService).chat("Merhaba");
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(aiAssistantService);
    }

    @Test
    void shouldReturnControlledErrorWhenAiServiceFails() throws Exception {
        when(aiAssistantService.chat("Merhaba"))
                .thenThrow(new AiServiceException(
                        "AI servisine şu anda ulaşılamıyor.",
                        new RuntimeException("Provider error")
                ));

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Merhaba"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.answer")
                        .value("AI servisine şu anda ulaşılamıyor."));

        verify(aiAssistantService).chat("Merhaba");
    }

    @Test
    void shouldRejectWhitespaceMessage() throws Exception {

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "message": "   "
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(aiAssistantService);
    }

}