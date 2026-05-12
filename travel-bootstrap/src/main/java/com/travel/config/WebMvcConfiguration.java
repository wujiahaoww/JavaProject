package com.travel.config;

import com.travel.common.properties.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p><b>作用：</b>扩展 Spring MVC 行为，当前主要负责按配置文件注册浏览器 / 小程序需要的 CORS（跨域）规则。</p>
 * <p>具体开关与域名列表来自 {@link AppProperties#getWeb()}，与 {@code application.yml} 中 {@code app.web.cors} 对应。</p>
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebMvcConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 当 CORS 启用且配置了至少一个允许来源时，对 {@code /api/**} 路径开放跨域访问。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var cors = appProperties.getWeb().getCors();
        // 未开启或未配置来源时不注册，避免误放开
        if (!cors.isEnabled() || cors.getAllowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(cors.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods(cors.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(cors.getAllowedHeaders().toArray(String[]::new))
                .allowCredentials(cors.isAllowCredentials());
    }
}
