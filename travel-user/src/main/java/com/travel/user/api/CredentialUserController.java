package com.travel.user.api;

import com.travel.common.api.ApiResult;
import com.travel.user.api.dto.LoginOtpRequest;
import com.travel.user.api.dto.LoginPasswordRequest;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.api.dto.RegisterCredentialRequest;
import com.travel.user.application.CredentialAuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>作用：</b>基于手机号 / 邮箱的注册与登录（密码或验证码），均返回与微信登录一致的 JWT 双令牌结构。</p>
 * <p>由 {@code @RestController} 注册，随 {@code com.travel} 包扫描加载。</p>
 */
@RestController
@RequestMapping("/api/v1/users")
public class CredentialUserController {

    private final CredentialAuthApplicationService credentialAuthApplicationService;

    public CredentialUserController(CredentialAuthApplicationService credentialAuthApplicationService) {
        this.credentialAuthApplicationService = credentialAuthApplicationService;
    }

    @PostMapping("/register/credential")
    public ApiResult<LoginResponse> register(@Valid @RequestBody RegisterCredentialRequest request) {
        return ApiResult.ok(credentialAuthApplicationService.register(
                request.channel(),
                request.contact(),
                request.code(),
                request.password(),
                request.confirmPassword()
        ));
    }

    @PostMapping("/login/password")
    public ApiResult<LoginResponse> loginPassword(@Valid @RequestBody LoginPasswordRequest request) {
        return ApiResult.ok(credentialAuthApplicationService.loginWithPassword(
                request.channel(),
                request.contact(),
                request.password()
        ));
    }

    @PostMapping("/login/code")
    public ApiResult<LoginResponse> loginCode(@Valid @RequestBody LoginOtpRequest request) {
        return ApiResult.ok(credentialAuthApplicationService.loginWithOtp(
                request.channel(),
                request.contact(),
                request.code()
        ));
    }
}
