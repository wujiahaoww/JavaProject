package com.travel.user.jwt;

/**
 * <p><b>作用：</b>封装一次「登录成功」或「刷新成功」后返回的一对 JWT 及元数据。</p>
 * <p>与 {@link com.travel.user.api.dto.LoginResponse} 对应：后者面向 HTTP JSON 字段命名（如 {@code expires_in} 风格）。</p>
 *
 * @param accessToken               短期访问令牌
 * @param refreshToken              长期刷新令牌
 * @param accessExpiresInSeconds    access 剩余有效秒数（给前端展示或定时刷新参考）
 * @param refreshExpiresInSeconds   refresh 剩余有效秒数
 * @param subject                   与 JWT {@code sub} 一致的用户标识
 */
public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessExpiresInSeconds,
        long refreshExpiresInSeconds,
        String subject
) {
}
