package com.travel.user.api;

import com.travel.common.api.ApiResult;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.api.dto.RefreshTokenRequest;
import com.travel.user.jwt.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p><b>作用：</b>认证相关 HTTP 接口（当前仅「刷新令牌」）。</p>
 * <p>刷新接口放在 JWT 过滤器白名单中：客户端只需携带 refresh，无需有效的 access。
 * 本控制器由 {@link com.travel.user.config.JwtKeyConfiguration} 在已配置 RSA 密钥时注册为 Bean，
 * 避免未配置密钥时仍暴露刷新端点却无法签发新令牌。</p>
 */
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 用合法的 refresh 令牌换取新的 access + refresh（旋转刷新，降低旧 refresh 泄露后的风险）。
     */
    @PostMapping("/refresh")
    @ResponseBody
    public ApiResult<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var pair = jwtTokenService.rotateFromRefresh(request.refreshToken());
        return ApiResult.ok(LoginResponse.from(pair));
    }
}
