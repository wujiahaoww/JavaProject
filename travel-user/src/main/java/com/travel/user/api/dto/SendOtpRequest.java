package com.travel.user.api.dto;

import com.travel.domain.user.ContactChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 请求发送验证码：小程序收集手机号/邮箱后调用；服务端先校验格式再限流、写 Redis。
 */
public record SendOtpRequest(
        @NotNull(message = "渠道不能为空") ContactChannel channel,
        @NotBlank(message = "手机号或邮箱不能为空") String contact
) {
}
