package com.travel.user.config;

import org.springframework.context.annotation.Configuration;

/**
 * <p><b>作用：</b>用户模块中与 JWT 相关的 Spring 配置占位类。</p>
 * <p>当前具体密钥加载、过滤器注册等已集中在 {@link JwtKeyConfiguration}；
 * 若将来需要拆分「令牌策略 Bean」或「多发行方」等，可在此类中追加 {@code @Bean}。</p>
 */
@Configuration
public class JwtTokenConfiguration {
}
