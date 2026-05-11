package com.travel.user.config;

import org.springframework.context.annotation.Configuration;

/**
 * <p><b>作用：</b>用户模块（travel-user）的装配入口占位。</p>
 * <p>可在此增加用户域专属的 {@code @Bean}、{@code @Import}、AOP、条件装配等；
 * 保持与 {@link JwtKeyConfiguration}、{@link JwtTokenConfiguration} 职责清晰分离。</p>
 */
@Configuration
public class UserModuleConfiguration {
}
