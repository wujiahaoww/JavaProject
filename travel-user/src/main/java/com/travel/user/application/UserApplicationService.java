package com.travel.user.application;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.domain.spi.WeChatSessionGateway;
import com.travel.user.api.dto.LoginResponse;
import com.travel.user.jwt.JwtTokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * <p><b>作用：</b>用户模块应用层服务：编排「微信登录 → 得到用户标识 → 签发 JWT」流程。</p>
 * <p>不直接依赖 HTTP，便于单测或将来换入口（如消息队列）；JWT 通过 {@link ObjectProvider}
 * 延迟获取，未配置密钥时不创建 {@link JwtTokenService} Bean，此处会抛出业务异常提示配置问题。</p>
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
     * 使用微信小程序 {@code jsCode} 换取 openid（或统一 subject），再签发 access + refresh。
     *
     * @param jsCode 前端传入的临时登录凭证
     * @return 与 OAuth 风格一致的双令牌响应体
     */
    public LoginResponse loginWithWeChat(String jsCode) {
        // 调用领域网关：真实环境对接微信 code2Session
        String subject = weChatSessionGateway.exchangeJsCode(jsCode);
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.WECHAT_LOGIN_FAILED);
        }
        // 仅当 RSA 密钥等就绪时才有 JwtTokenService Bean
        JwtTokenService jwt = jwtTokenService.getIfAvailable();
        if (jwt == null) {
            throw new BusinessException(GlobalErrorCode.JWT_NOT_CONFIGURED);
        }
        String sub = subject.trim();
        return LoginResponse.from(jwt.issuePair(sub));
    }
}
