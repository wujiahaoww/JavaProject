package com.travel.user.otp;

import com.travel.domain.user.ContactChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p><b>作用：</b>开发联调用的验证码投递：将验证码打到日志，便于无短信网关时联调。</p>
 * <p><b>生产务必替换</b>为真实 SMS / 邮件实现，并关闭明文日志。</p>
 */
@Component
public class LoggingOtpDeliveryNotifier implements OtpDeliveryNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingOtpDeliveryNotifier.class);

    @Override
    public void deliver(ContactChannel channel, String contact, String code) {
        log.warn("[OTP-DEV] channel={} contact={} code={} （生产请删除此类日志并对接网关）", channel, mask(contact), code);
    }

    private static String mask(String contact) {
        if (contact == null || contact.length() < 5) {
            return "****";
        }
        return contact.substring(0, 3) + "****" + contact.substring(contact.length() - 2);
    }
}
