package com.travel.user.application;

import com.travel.domain.spi.WeChatSessionGateway;
import org.springframework.stereotype.Service;

/**
 * 用户应用服务（占位：登录编排、资料更新等）。
 */
@Service
public class UserApplicationService {

    private final WeChatSessionGateway weChatSessionGateway;

    public UserApplicationService(WeChatSessionGateway weChatSessionGateway) {
        this.weChatSessionGateway = weChatSessionGateway;
    }

    public String loginByJsCode(String jsCode) {
        return weChatSessionGateway.exchangeJsCode(jsCode);
    }
}
