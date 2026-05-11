package com.travel.config;

import com.travel.common.properties.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 全局扩展：按 {@link AppProperties#getWeb()} 注册 CORS，便于小程序 / 浏览器跨域联调。
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebMvcConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var cors = appProperties.getWeb().getCors();
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
