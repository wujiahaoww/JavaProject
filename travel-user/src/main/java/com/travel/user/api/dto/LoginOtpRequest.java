package com.travel.user.api.dto;

import com.travel.domain.user.ContactChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 使用手机号或邮箱 + 验证码登录（无需密码）。 */
public record LoginOtpRequest(
        @NotNull(message = "渠道不能为空") ContactChannel channel,
        @NotBlank(message = "手机号或邮箱不能为空") String contact,
        @NotBlank(message = "验证码不能为空") String code
) {
}
