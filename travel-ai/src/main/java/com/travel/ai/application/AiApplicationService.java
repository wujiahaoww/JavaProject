package com.travel.ai.application;

import com.travel.domain.spi.LlmClient;
import org.springframework.stereotype.Service;

/**
 * AI 应用服务（占位：会话上下文、Prompt 组装、落库等）。
 */
@Service
public class AiApplicationService {

    private final LlmClient llmClient;

    public AiApplicationService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public String chat(String sessionId, String userMessage) {
        return llmClient.chat(sessionId, userMessage);
    }

    public String guide(String destination, int days, String budgetHint) {
        return llmClient.generateTravelGuide(destination, days, budgetHint);
    }
}
