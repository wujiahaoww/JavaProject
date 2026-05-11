package com.travel;

import com.travel.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 应用入口：扫描 com.travel 下全部 Spring 组件。
 */
@SpringBootApplication(scanBasePackages = "com.travel")
@EnableConfigurationProperties(AppProperties.class)
public class TravelAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelAssistantApplication.class, args);
    }
}
