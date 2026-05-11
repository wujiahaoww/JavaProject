package com.travel.user.api.dto;

import com.travel.user.jwt.TokenPair;

/**
 * <p><b>作用：</b>登录或刷新成功后返回给前端的 JSON 结构，字段命名贴近常见 OAuth2 资源响应（如 token_type、expires_in）。</p>
 *
 * @param accessToken      访问令牌，请求受保护 API 时放在 {@code Authorization: Bearer ...}
 * @param refreshToken     刷新令牌，仅用于调用 {@code POST /api/v1/auth/refresh}
 * @param tokenType        固定为 {@code Bearer}，便于客户端按标准库解析
 * @param expiresIn        access 有效剩余秒数
 * @param refreshExpiresIn refresh 有效剩余秒数
 * @param subject          与 JWT {@code sub} 一致的用户标识
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        String subject
) {
    /**
     * 从内部 {@link TokenPair} 转为对外 DTO（补充 tokenType 固定值）。
     */
    public static LoginResponse from(TokenPair pair) {
        return new LoginResponse(
                pair.accessToken(),
                pair.refreshToken(),
                "Bearer",
                pair.accessExpiresInSeconds(),
                pair.refreshExpiresInSeconds(),
                pair.subject()
        );
    }
}
