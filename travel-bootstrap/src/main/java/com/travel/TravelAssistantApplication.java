package com.travel;

import com.travel.common.properties.AppProperties;
import com.travel.integration.wechat.WeChatMiniProgramProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 应用入口：扫描 com.travel 下全部 Spring 组件。
 */
@SpringBootApplication(scanBasePackages = "com.travel")
@EnableConfigurationProperties({AppProperties.class, WeChatMiniProgramProperties.class})
@MapperScan("com.travel.user.infrastructure.mapper")
public class TravelAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAssistantApplication.class, args);
    }
}
