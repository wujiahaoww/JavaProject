package com.travel.user.application;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.common.properties.UserSecurityProperties;
import com.travel.domain.user.ContactChannel;
import com.travel.domain.user.UserAccount;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.infrastructure.mapper.UserAccountMapper;
import com.travel.user.jwt.JwtTokenService;
import com.travel.user.otp.OtpDeliveryNotifier;
import com.travel.user.otp.OtpRedisService;
import com.travel.user.support.ContactFormatValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p><b>作用：</b>手机号 / 邮箱维度的注册与登录（密码或验证码），并与 JWT 签发衔接。</p>
 * <p>验证码存 Redis、防刷与锁定逻辑见 {@link OtpRedisService}；格式校验见 {@link ContactFormatValidator}。</p>
 */
@Service
public class CredentialAuthApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final ObjectProvider<OtpRedisService> otpRedisService;
    private final ObjectProvider<JwtTokenService> jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserSecurityProperties userSecurityProperties;
    private final OtpDeliveryNotifier otpDeliveryNotifier;

    public CredentialAuthApplicationService(
            UserAccountMapper userAccountMapper,
            ObjectProvider<OtpRedisService> otpRedisService,
            ObjectProvider<JwtTokenService> jwtTokenService,
            PasswordEncoder passwordEncoder,
            UserSecurityProperties userSecurityProperties,
            OtpDeliveryNotifier otpDeliveryNotifier
    ) {
        this.userAccountMapper = userAccountMapper;
        this.otpRedisService = otpRedisService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.userSecurityProperties = userSecurityProperties;
        this.otpDeliveryNotifier = otpDeliveryNotifier;
    }

    /**
     * 发送验证码：先校验联系方式格式，再限流、落 Redis，最后通过 {@link OtpDeliveryNotifier} 投递（开发为日志）。
     */
    public void sendOtp(ContactChannel channel, String rawContact, String clientIp) {
        OtpRedisService otp = requireOtpService();
        String contact = ContactFormatValidator.validateAndNormalize(channel, rawContact);
        String ip = StringUtils.hasText(clientIp) ? clientIp.trim() : "unknown";
        String code = otp.createAndStore(channel, contact, ip);
        otpDeliveryNotifier.deliver(channel, contact, code);
    }

    /**
     * 注册：校验验证码后写入用户与密码哈希，并签发双令牌。
     */
    public LoginResponse register(ContactChannel channel, String rawContact, String code, String password, String confirmPassword) {
        assertPasswordPolicy(password, confirmPassword);
        String contact = ContactFormatValidator.validateAndNormalize(channel, rawContact);
        requireOtpService().verifyAndConsume(channel, contact, code);

        if (findByChannel(channel, contact) != null) {
            throw new BusinessException(GlobalErrorCode.USER_ALREADY_EXISTS);
        }

        UserAccount account = new UserAccount();
        if (channel == ContactChannel.PHONE) {
            account.setPhone(contact);
        } else {
            account.setEmail(contact);
        }
        account.setPasswordHash(passwordEncoder.encode(password));
        userAccountMapper.insert(account);
        if (account.getId() == null) {
            throw new BusinessException(GlobalErrorCode.INTERNAL_ERROR, "注册失败，请稍后重试");
        }
        return issueCredentialTokens(account.getId());
    }

    /**
     * 密码登录：校验账号存在且密码匹配后签发双令牌。
     */
    public LoginResponse loginWithPassword(ContactChannel channel, String rawContact, String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "密码不能为空");
        }
        String contact = ContactFormatValidator.validateAndNormalize(channel, rawContact);
        UserAccount user = findByChannel(channel, contact);
        if (user == null || !StringUtils.hasText(user.getPasswordHash())) {
            throw new BusinessException(GlobalErrorCode.INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(GlobalErrorCode.INVALID_CREDENTIALS);
        }
        return issueCredentialTokens(user.getId());
    }

    /**
     * 验证码登录：校验验证码后，用户必须已存在，再签发双令牌。
     */
    public LoginResponse loginWithOtp(ContactChannel channel, String rawContact, String code) {
        String contact = ContactFormatValidator.validateAndNormalize(channel, rawContact);
        requireOtpService().verifyAndConsume(channel, contact, code);
        UserAccount user = findByChannel(channel, contact);
        if (user == null) {
            throw new BusinessException(GlobalErrorCode.USER_NOT_FOUND);
        }
        return issueCredentialTokens(user.getId());
    }

    private OtpRedisService requireOtpService() {
        OtpRedisService otp = otpRedisService.getIfAvailable();
        if (otp == null) {
            throw new BusinessException(GlobalErrorCode.REDIS_NOT_AVAILABLE);
        }
        return otp;
    }

    private UserAccount findByChannel(ContactChannel channel, String normalizedContact) {
        return switch (channel) {
            case PHONE -> userAccountMapper.findByPhone(normalizedContact);
            case EMAIL -> userAccountMapper.findByEmail(normalizedContact);
        };
    }

    private void assertPasswordPolicy(String password, String confirmPassword) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "密码不能为空");
        }
        int min = userSecurityProperties.getPasswordMinLength();
        if (password.length() < min) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "密码长度不能少于 " + min + " 位");
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "两次输入的密码不一致");
        }
    }

    /**
     * 凭证用户 JWT subject 统一为 {@code uid:数据库主键}，与微信 openid 字符串区分。
     */
    private LoginResponse issueCredentialTokens(Long userId) {
        JwtTokenService jwt = jwtTokenService.getIfAvailable();
        if (jwt == null) {
            throw new BusinessException(GlobalErrorCode.JWT_NOT_CONFIGURED);
        }
        return LoginResponse.from(jwt.issuePair("uid:" + userId));
    }
}
