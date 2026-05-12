package com.travel.user.otp;

import com.travel.domain.user.ContactChannel;

/**
 * <p><b>作用：</b>验证码「投递」抽象：生产应对接短信 / 邮件网关；小程序侧仅负责收集手机号/邮箱并调后端发码。</p>
 */
public interface OtpDeliveryNotifier {

    /**
     * 将验证码投递给用户（短信、邮件或开发日志）。
     *
     * @param channel 渠道
     * @param contact 已归一化的手机号或邮箱
     * @param code    明文验证码（生产环境勿写入 INFO 日志）
     */
    void deliver(ContactChannel channel, String contact, String code);
}
