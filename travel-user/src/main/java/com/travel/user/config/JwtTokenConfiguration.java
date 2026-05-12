package com.travel.user.config;

import org.springframework.context.annotation.Configuration;

/**
 * <p><b>作用：</b>用户模块 JWT 能力的<strong>装配入口说明</strong>（本类本身不声明 Bean）。</p>
 * <p>实际 RSA 密钥加载、{@code JwtTokenService}、{@code AuthController}、{@code JwtAuthenticationFilter}
 * 均在 {@link JwtKeyConfiguration} 中按条件注册（需配置 {@code app.jwt.keys.private-location} 与
 * {@code app.jwt.keys.public-location}，且勿使用空字符串占位）。实际签发与 refresh 的 Redis 绑定依赖
 * {@link JwtTokenService}（需 {@code StringRedisTemplate}，由 Spring Data Redis 自动配置提供）。</p>
 * <p>算法：<strong>RS256</strong>；令牌在「微信登录 / 注册并登录」与「刷新」接口中发放，详见 {@code application.yml} 中 {@code app.jwt} 注释。</p>
 */
@Configuration
public class JwtTokenConfiguration {
}
