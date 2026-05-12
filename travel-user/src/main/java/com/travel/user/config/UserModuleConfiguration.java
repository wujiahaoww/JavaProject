package com.travel.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p><b>作用：</b>用户模块（travel-user）的装配：密码哈希等跨接口复用 Bean。</p>
 * <p>与 {@link JwtKeyConfiguration}、{@link JwtTokenConfiguration} 职责分离：本类不依赖 RSA 是否配置。</p>
 */
@Configuration
public class UserModuleConfiguration {

    /**
     * BCrypt 密码编码器，用于注册与密码登录校验。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
