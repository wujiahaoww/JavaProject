package com.travel.user.application;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.domain.spi.WeChatSessionGateway;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.jwt.JwtTokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * <p><b>作用：</b>用户模块应用层服务：编排「微信身份 → 签发 JWT（RS256，私钥签 / 公钥验）」流程。</p>
 * <p>小程序常见模式为<strong>无独立注册页</strong>：首次 {@code jsCode} 换到 openid 即视为注册，
 * 与再次登录共用同一套签发逻辑；因此 {@link #loginWithWeChat(String)} 与 {@link #registerWithWeChat(String)} 行为一致，
 * 仅语义上区分「登录入口」与「注册并登录入口」。</p>
 * <p>JWT 通过 {@link ObjectProvider} 延迟获取：未配置 RSA 密钥时不存在 {@link JwtTokenService} Bean，
 * 此处抛出 {@link GlobalErrorCode#JWT_NOT_CONFIGURED}。</p>
 */
@Service
public class UserApplicationService {

    private final WeChatSessionGateway weChatSessionGateway;
    private final ObjectProvider<JwtTokenService> jwtTokenService;

    public UserApplicationService(WeChatSessionGateway weChatSessionGateway,
                                  ObjectProvider<JwtTokenService> jwtTokenService) {
        this.weChatSessionGateway = weChatSessionGateway;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 微信<strong>登录</strong>：{@code jsCode} → subject（如 openid）→ 签发 access + refresh（登录阶段发令牌）。
     *
     * @param jsCode 前端 {@code wx.login} 获得的临时凭证
     * @return 双令牌响应（{@code tokenType} 为 Bearer）
     */
    public LoginResponse loginWithWeChat(String jsCode) {
        return issueTokensAfterWeChat(jsCode);
    }

    /**
     * 微信<strong>注册并登录</strong>：与 {@link #loginWithWeChat(String)} 相同实现，
     * 满足「注册 / 登录阶段均需发放 JWT」的接口划分；首次与再次调用均由网关与持久层决定是否为新建用户。
     *
     * @param jsCode 前端 {@code wx.login} 获得的临时凭证
     * @return 双令牌响应
     */
    public LoginResponse registerWithWeChat(String jsCode) {
        return issueTokensAfterWeChat(jsCode);
    }

    /**
     * 微信 code 换身份后签发 RS256 双令牌（access 短期 + refresh 长期）。
     */
    private LoginResponse issueTokensAfterWeChat(String jsCode) {
        // 调用领域网关：对接微信 jscode2session，得到稳定 subject（如 openid）
        String subject = weChatSessionGateway.exchangeJsCode(jsCode);
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.WECHAT_LOGIN_FAILED);
        }
        JwtTokenService jwt = jwtTokenService.getIfAvailable();
        if (jwt == null) {
            throw new BusinessException(GlobalErrorCode.JWT_NOT_CONFIGURED);
        }
        String sub = subject.trim();
        return LoginResponse.from(jwt.issuePair(sub));
    }
}
