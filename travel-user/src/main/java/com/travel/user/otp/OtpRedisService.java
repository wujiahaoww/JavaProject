package com.travel.user.otp;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.common.properties.UserSecurityProperties;
import com.travel.domain.user.ContactChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * <p><b>作用：</b>验证码的生成、写入 Redis、发送频控、输错锁定与校验消费。</p>
 * <p>通过 {@code @Service} 随 {@code com.travel} 包扫描注册；依赖 {@link StringRedisTemplate} 构造器注入，
 * 由 Spring 在依赖解析阶段创建，<strong>不使用</strong>{@code @ConditionalOnBean}，避免与 Redis 自动配置刷新顺序冲突。</p>
 * <p>若工程未启用 Redis（无 {@code StringRedisTemplate}），启动将因缺少依赖失败，需在 bootstrap 中保留
 * {@code spring-boot-starter-data-redis} 与 {@code spring.data.redis} 配置。</p>
 */
@Service
public class OtpRedisService {

    private static final Logger log = LoggerFactory.getLogger(OtpRedisService.class);
    private static final String P = "travel:otp:";

    private final StringRedisTemplate redis;
    private final UserSecurityProperties securityProperties;
    private final SecureRandom random = new SecureRandom();

    public OtpRedisService(StringRedisTemplate redis, UserSecurityProperties securityProperties) {
        this.redis = redis;
        this.securityProperties = securityProperties;
    }

    /**
     * 生成数字验证码、写入 Redis，并做冷却、按联系方式、按 IP 的限流检查。
     *
     * @param channel 手机或邮箱
     * @param contact 已归一化的联系方式
     * @param clientIp 客户端 IP（用于防刷）
     * @return 明文验证码（仅用于开发环境日志投递；生产应对接短信网关，勿打日志）
     */
    public String createAndStore(ContactChannel channel, String contact, String clientIp) {
        assertNotLocked(channel, contact);

        var otp = securityProperties.getOtp();
        String cooldownKey = P + "cooldown:" + channel.name() + ":" + contact;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            throw new BusinessException(GlobalErrorCode.OTP_SEND_COOLDOWN);
        }

        String dayKey = P + "send-day:" + channel.name() + ":" + contact;
        Long dayCount = redis.opsForValue().increment(dayKey);
        if (dayCount != null && dayCount == 1L) {
            redis.expire(dayKey, Duration.ofHours(25));
        }
        if (dayCount != null && dayCount > otp.getDailyMaxSendsPerContact()) {
            redis.opsForValue().decrement(dayKey);
            throw new BusinessException(GlobalErrorCode.OTP_SEND_LIMIT_CONTACT);
        }

        String ipKey = P + "send-ip:" + clientIp;
        Long ipCount = redis.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1L) {
            redis.expire(ipKey, Duration.ofHours(1));
        }
        if (ipCount != null && ipCount > otp.getHourlyMaxSendsPerIp()) {
            redis.opsForValue().decrement(ipKey);
            throw new BusinessException(GlobalErrorCode.OTP_SEND_LIMIT_IP);
        }

        String code = randomDigits(otp.getCodeLength());
        String codeKey = P + "code:" + channel.name() + ":" + contact;
        redis.opsForValue().set(codeKey, code, otp.getTtl());

        // 新验证码周期：清空历史输错次数
        redis.delete(P + "fail:" + channel.name() + ":" + contact);

        redis.opsForValue().set(cooldownKey, "1", otp.getSendCooldown());

        log.debug("OTP generated for {} {}", channel, mask(contact));
        return code;
    }

    /**
     * 校验验证码：正确则删除验证码及失败计数；错误则累加失败次数，达到阈值则锁定并删除验证码。
     */
    public void verifyAndConsume(ContactChannel channel, String contact, String inputCode) {
        assertNotLocked(channel, contact);
        if (inputCode == null || inputCode.isBlank()) {
            throw new BusinessException(GlobalErrorCode.OTP_INVALID);
        }

        var otp = securityProperties.getOtp();
        String codeKey = P + "code:" + channel.name() + ":" + contact;
        String expected = redis.opsForValue().get(codeKey);
        if (expected == null) {
            throw new BusinessException(GlobalErrorCode.OTP_INVALID);
        }

        if (constantTimeEquals(expected, inputCode.trim())) {
            redis.delete(codeKey);
            redis.delete(P + "fail:" + channel.name() + ":" + contact);
            return;
        }

        String failKey = P + "fail:" + channel.name() + ":" + contact;
        Long fails = redis.opsForValue().increment(failKey);
        if (fails != null && fails == 1L) {
            redis.expire(failKey, otp.getTtl());
        }
        if (fails != null && fails >= otp.getMaxVerifyFailures()) {
            String lockKey = P + "lock:" + channel.name() + ":" + contact;
            redis.opsForValue().set(lockKey, "1", otp.getLockDuration());
            redis.delete(codeKey);
            redis.delete(failKey);
            throw new BusinessException(GlobalErrorCode.OTP_LOCKED);
        }
        throw new BusinessException(GlobalErrorCode.OTP_INVALID);
    }

    private void assertNotLocked(ContactChannel channel, String contact) {
        String lockKey = P + "lock:" + channel.name() + ":" + contact;
        if (Boolean.TRUE.equals(redis.hasKey(lockKey))) {
            throw new BusinessException(GlobalErrorCode.OTP_LOCKED);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }

    private String randomDigits(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static String mask(String contact) {
        if (contact == null || contact.length() < 4) {
            return "****";
        }
        return contact.substring(0, 2) + "****" + contact.substring(contact.length() - 2);
    }
}
