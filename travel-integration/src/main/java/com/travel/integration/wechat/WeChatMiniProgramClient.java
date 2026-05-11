package com.travel.integration.wechat;

import com.travel.domain.spi.WeChatSessionGateway;

/**
 * 微信小程序会话网关占位实现（后续调用 jscode2session）。
 */
public class WeChatMiniProgramClient implements WeChatSessionGateway {

    @Override
    public String exchangeJsCode(String jsCode) {
        return "";
    }
}
