package com.travel.user.api;

import com.travel.common.api.ApiResult;
import com.travel.user.api.dto.SendOtpRequest;
import com.travel.user.application.CredentialAuthApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>作用：</b>验证码发送入口（无需登录）。</p>
 * <p>流程说明：小程序页面取得用户同意后的手机号或邮箱 → 调本接口 → 服务端校验格式、防刷后写入 Redis，
 * 再通过 {@link com.travel.user.otp.OtpDeliveryNotifier} 投递（开发环境为日志；生产应对接短信/邮件网关）。</p>
 * <p>由 {@code @RestController} 注册，随启动类 {@code scanBasePackages = "com.travel"} 一并扫描。</p>
 */
@RestController
@RequestMapping("/api/v1/auth/otp")
public class OtpAuthController {

    private final CredentialAuthApplicationService credentialAuthApplicationService;

    public OtpAuthController(CredentialAuthApplicationService credentialAuthApplicationService) {
        this.credentialAuthApplicationService = credentialAuthApplicationService;
    }

    @PostMapping("/send")
    public ApiResult<Void> send(@Valid @RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        credentialAuthApplicationService.sendOtp(request.channel(), request.contact(), resolveClientIp(httpRequest));
        return ApiResult.ok();
    }

    /**
     * 优先取反向代理透传的真实 IP，否则使用直连地址。
     */
    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
