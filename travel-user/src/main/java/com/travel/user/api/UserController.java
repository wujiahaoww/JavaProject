package com.travel.user.api;

import com.travel.common.api.ApiResult;
import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.api.dto.UserProfileResponse;
import com.travel.user.application.UserApplicationService;
import com.travel.user.security.LoginUserHolder;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>作用：</b>用户域对外 REST 接口：微信登录、查询当前登录用户资料等。</p>
 * <p>除登录接口外，其它路径通常需先经过 {@link com.travel.user.security.JwtAuthenticationFilter}
 * 校验 access 令牌，再通过 {@link LoginUserHolder} 取得当前用户标识。</p>
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 获取当前登录用户最小资料（subject 来自 JWT 的 {@code sub}）。
     * 若未登录或过滤器未设置 subject，则返回未授权业务错误。
     */
    @GetMapping("/me")
    public ApiResult<UserProfileResponse> me() {
        // 从当前请求线程读取 JWT 解析后的用户标识
        String subject = LoginUserHolder.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.UNAUTHORIZED);
        }
        return ApiResult.ok(new UserProfileResponse(subject));
    }

    /**
     * 微信<strong>登录</strong>：用 {@code jsCode} 换 subject 后签发 RS256 的 access + refresh（登录阶段发令牌）。
     * 请求体：{@code { "jsCode": "..." }}。
     */
    @PostMapping("/login/wechat")
    public ApiResult<LoginResponse> wechatLogin(@RequestBody WechatLoginRequest request) {
        return ApiResult.ok(userApplicationService.loginWithWeChat(request.jsCode()));
    }

    /**
     * 微信<strong>注册并登录</strong>：与 {@link #wechatLogin} 业务一致，便于前端区分「首次进入」与「老用户登录」路由；
     * 服务端均在身份校验通过后发放双令牌。
     */
    @PostMapping("/register/wechat")
    public ApiResult<LoginResponse> wechatRegister(@RequestBody WechatLoginRequest request) {
        return ApiResult.ok(userApplicationService.registerWithWeChat(request.jsCode()));
    }

    /**
     * 微信登录请求体：仅包含小程序端传来的 {@code jsCode}。
     *
     * @param jsCode 微信接口换取 openid 所需的临时凭证
     */
    public record WechatLoginRequest(@NotBlank String jsCode) {
    }
}
