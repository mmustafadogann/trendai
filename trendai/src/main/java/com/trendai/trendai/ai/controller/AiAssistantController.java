package com.trendai.trendai.ai.controller;

import com.trendai.trendai.ai.dto.ChatRequest;
import com.trendai.trendai.ai.dto.ChatResponse;
import com.trendai.trendai.ai.service.AiAssistantService;
import com.trendai.trendai.ai.service.AiServiceException;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@ConditionalOnProperty(
        name = "app.ai.enabled",
        havingValue = "true"
)
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        try {
            String answer = aiAssistantService.chat(request.message());

            return ResponseEntity.ok(
                    new ChatResponse(
                            answer,
                            aiAssistantService.getMode()
                    )
            );

        } catch (AiServiceException e) {
            return ResponseEntity.internalServerError()
                    .body(
                            new ChatResponse(
                                    e.getMessage(),
                                    "ERROR"
                            )
                    );
        }
    }
}