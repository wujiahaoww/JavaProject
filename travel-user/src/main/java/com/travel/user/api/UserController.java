package com.travel.user.api;

import com.travel.common.api.ApiResult;
import com.travel.user.application.UserApplicationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关 API（占位）。
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping("/me")
    public ApiResult<Void> me() {
        return ApiResult.ok();
    }

    @PostMapping("/login/wechat")
    public ApiResult<String> wechatLogin(@RequestBody WechatLoginRequest request) {
        String sessionSummary = userApplicationService.loginByJsCode(request.jsCode());
        return ApiResult.ok(sessionSummary);
    }

    public record WechatLoginRequest(@NotBlank String jsCode) {
    }
}
