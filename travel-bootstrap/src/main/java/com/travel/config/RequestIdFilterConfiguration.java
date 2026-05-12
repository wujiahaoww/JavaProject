package com.travel.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * <p><b>作用：</b>把 {@link RequestIdFilter} 注册进 Servlet 容器，并设置较高优先级。</p>
 * <p>顺序设为 {@code HIGHEST_PRECEDENCE + 10}，保证在 JWT 等业务过滤器之前执行，使后续过滤器与控制器日志都能带上 requestId。</p>
 */
@Configuration
public class RequestIdFilterConfiguration {

    /**
     * 注册请求 ID 过滤器：匹配所有路径 {@code /*}。
     */
    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("requestIdFilter");
        return registration;
    }
}
