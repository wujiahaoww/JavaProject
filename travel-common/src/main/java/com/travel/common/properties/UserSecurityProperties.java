package com.travel.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * <p><b>作用：</b>绑定 {@code app.user.security}，控制验证码长度、有效期、防刷与锁定策略。</p>
 * <p>验证码实际存储在 Redis（见 user 模块 {@code OtpRedisService}）；真实短信/邮件需对接第三方，开发环境可只看日志。</p>
 */
@ConfigurationProperties(prefix = "app.user.security")
public class UserSecurityProperties {

    /**
     * 密码登录 / 注册时的密码长度下限（仅服务端校验，前端应同步提示）。
     */
    private int passwordMinLength = 8;

    /**
     * 验证码相关参数。
     */
    @NestedConfigurationProperty
    private final Otp otp = new Otp();

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    public Otp getOtp() {
        return otp;
    }

    /**
     * 对应配置节 {@code app.user.security.otp}。
     */
    public static class Otp {

        /** 数字验证码位数（默认 6） */
        private int codeLength = 6;

        /** 验证码在 Redis 中的存活时间（过期后需重新获取） */
        private Duration ttl = Duration.ofMinutes(5);

        /**
         * 连续输错 {@link #maxVerifyFailures} 次后，禁止再校验/发送的锁定时长（与需求「锁定 5 分钟」对齐）。
         */
        private Duration lockDuration = Duration.ofMinutes(5);

        /** 同一手机号/邮箱，校验失败累计达到该次数则触发锁定 */
        private int maxVerifyFailures = 5;

        /** 两次发送验证码之间的最短间隔（防轰炸） */
        private Duration sendCooldown = Duration.ofSeconds(60);

        /** 同一联系方式 24 小时内最多发送次数（滑动窗口，用 Redis TTL≈24h 实现） */
        private int dailyMaxSendsPerContact = 10;

        /** 同一客户端 IP 1 小时内最多发送次数 */
        private int hourlyMaxSendsPerIp = 30;

        public int getCodeLength() {
            return codeLength;
        }

        public void setCodeLength(int codeLength) {
            this.codeLength = codeLength;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public Duration getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Duration lockDuration) {
            this.lockDuration = lockDuration;
        }

        public int getMaxVerifyFailures() {
            return maxVerifyFailures;
        }

        public void setMaxVerifyFailures(int maxVerifyFailures) {
            this.maxVerifyFailures = maxVerifyFailures;
        }

        public Duration getSendCooldown() {
            return sendCooldown;
        }

        public void setSendCooldown(Duration sendCooldown) {
            this.sendCooldown = sendCooldown;
        }

        public int getDailyMaxSendsPerContact() {
            return dailyMaxSendsPerContact;
        }

        public void setDailyMaxSendsPerContact(int dailyMaxSendsPerContact) {
            this.dailyMaxSendsPerContact = dailyMaxSendsPerContact;
        }

        public int getHourlyMaxSendsPerIp() {
            return hourlyMaxSendsPerIp;
        }

        public void setHourlyMaxSendsPerIp(int hourlyMaxSendsPerIp) {
            this.hourlyMaxSendsPerIp = hourlyMaxSendsPerIp;
        }
    }
}
