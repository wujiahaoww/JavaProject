package com.travel;

import com.travel.common.properties.AppProperties;
import com.travel.integration.wechat.WeChatMiniProgramProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * <p><b>作用：</b>整个「智能旅行助手」后端的 Spring Boot 启动入口，负责拉起内嵌 Tomcat、加载各业务模块 Bean。</p>
 * <ul>
 *   <li>{@code @SpringBootApplication}：自动配置 + 组件扫描，限定在 {@code com.travel} 包及其子包。</li>
 *   <li>{@code @EnableConfigurationProperties}：把 {@code application.yml} 里 {@code app.*}、微信等前缀绑定到 Java 配置类。</li>
 *   <li>{@code @MapperScan}：告诉 MyBatis 去扫描用户模块下的 Mapper 接口并生成实现代理。</li>
 * </ul>
 */
@SpringBootApplication(scanBasePackages = "com.travel")
@EnableConfigurationProperties({AppProperties.class, WeChatMiniProgramProperties.class})
@MapperScan("com.travel.user.infrastructure.mapper")
public class TravelAssistantApplication {

    /**
     * JVM 入口：创建 Spring 应用上下文并启动 Web 容器。
     *
     * @param args 命令行参数（可传 {@code --spring.profiles.active=prod} 等）
     */
    public static void main(String[] args) {
        SpringApplication.run(TravelAssistantApplication.class, args);
    }
}
