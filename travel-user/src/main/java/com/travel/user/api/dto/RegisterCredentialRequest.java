package com.travel.user.api.dto;

import com.travel.domain.user.ContactChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 手机号或邮箱注册：需先调用发送验证码，用户在微信侧收到短信/邮件后填写 code。
 */
public record RegisterCredentialRequest(
        @NotNull(message = "渠道不能为空") ContactChannel channel,
        @NotBlank(message = "手机号或邮箱不能为空") String contact,
        @NotBlank(message = "验证码不能为空") String code,
        @NotBlank(message = "密码不能为空") String password,
        @NotBlank(message = "确认密码不能为空") String confirmPassword
) {
}
