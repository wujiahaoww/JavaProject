package com.travel.domain.spi;

/**
 * 大模型调用 SPI，由 integration 模块实现。
 */
public interface LlmClient {

    String chat(String sessionId, String userMessage);

    String generateTravelGuide(String destination, int days, String budgetHint);
}
