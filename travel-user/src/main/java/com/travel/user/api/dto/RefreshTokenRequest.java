package com.travel.user.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * <p><b>作用：</b>客户端调用 {@code POST /api/v1/auth/refresh} 时的请求体。</p>
 *
 * @param refreshToken 当前持有的 refresh JWT，不能为空字符串
 */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
