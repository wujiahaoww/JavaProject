package com.travel.user.support;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.domain.user.ContactChannel;

import java.util.regex.Pattern;

/**
 * <p><b>作用：</b>在发送验证码、注册、登录前校验手机号或邮箱格式，避免无效请求打 Redis / 下游。</p>
 */
public final class ContactFormatValidator {

    /** 中国大陆 11 位手机号，1 开头第二位 3–9 */
    private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    /** 宽松邮箱格式（生产可换更严格校验或第三方库） */
    private static final Pattern EMAIL = Pattern.compile("^[\\w.!#$%&'*+/=?^`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    private ContactFormatValidator() {
    }

    /**
     * @param channel 手机或邮箱
     * @param raw     用户输入原文（手机号可先含空格，内部会归一）
     */
    public static String validateAndNormalize(ContactChannel channel, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "手机号或邮箱不能为空");
        }
        return switch (channel) {
            case PHONE -> validatePhone(raw);
            case EMAIL -> validateEmail(raw);
        };
    }

    private static String validatePhone(String raw) {
        String digits = raw.replaceAll("\\D", "");
        if (!CN_MOBILE.matcher(digits).matches()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        return digits;
    }

    private static String validateEmail(String raw) {
        String email = raw.trim().toLowerCase();
        if (email.length() > 128 || !EMAIL.matcher(email).matches()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "邮箱格式不正确");
        }
        return email;
    }
}
