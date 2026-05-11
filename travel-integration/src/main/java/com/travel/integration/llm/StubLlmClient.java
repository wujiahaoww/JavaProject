package com.travel.integration.llm;

import com.travel.domain.spi.LlmClient;

/**
 * 大模型占位实现（后续接入通义/文心等）。
 */
public class StubLlmClient implements LlmClient {

    @Override
    public String chat(String sessionId, String userMessage) {
        return "";
    }

    @Override
    public String generateTravelGuide(String destination, int days, String budgetHint) {
        return "";
    }
}
