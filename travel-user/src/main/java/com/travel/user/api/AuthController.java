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
 * <p><b>作用：</b>认证相关 HTTP 接口（刷新 access / refresh 双令牌）。</p>
 * <p>使用 <strong>RS256</strong>：refresh 与 access 一样由服务端私钥签发，刷新时用公钥验签；
 * 新令牌仍由私钥签名（见 {@link com.travel.user.jwt.JwtTokenService}）。</p>
 * <p>本接口在 JWT 过滤器<strong>白名单</strong>中：请求体携带 refresh 即可，无需 {@code Authorization: Bearer} access。</p>
 * <p>由 {@link com.travel.user.config.JwtKeyConfiguration} 在已配置 RSA PEM 路径时注册，避免无密钥时暴露无效端点。</p>
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
