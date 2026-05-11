package com.travel.integration.config;

import com.travel.domain.spi.HotelSupplier;
import com.travel.domain.spi.LlmClient;
import com.travel.domain.spi.WeChatSessionGateway;
import com.travel.integration.hotel.StubHotelSupplier;
import com.travel.integration.llm.StubLlmClient;
import com.travel.integration.wechat.WeChatMiniProgramClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方 SPI 实现注册（组合根的一部分，物理上放在 integration 模块）。
 */
@Configuration
public class IntegrationConfiguration {

    @Bean
    public HotelSupplier hotelSupplier() {
        return new StubHotelSupplier();
    }

    @Bean
    public LlmClient llmClient() {
        return new StubLlmClient();
    }

    @Bean
    public WeChatSessionGateway weChatSessionGateway() {
        return new WeChatMiniProgramClient();
    }
}
