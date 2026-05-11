package com.travel.user.api.dto;

/**
 * <p><b>作用：</b>{@code GET /api/v1/users/me} 返回的当前用户最小信息。</p>
 * <p>目前仅包含与 JWT {@code sub} 一致的 {@code subject}；后续可扩展昵称、头像 URL 等字段。</p>
 *
 * @param subject 当前登录用户标识（openid 或内部 userId）
 */
public record UserProfileResponse(String subject) {
}
