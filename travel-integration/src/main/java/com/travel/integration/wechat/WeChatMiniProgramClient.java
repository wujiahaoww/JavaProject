package com.travel.integration.wechat;

import com.travel.domain.spi.WeChatSessionGateway;

/**
 * 微信小程序会话网关占位实现（后续用 {@link WeChatMiniProgramProperties} 调 jscode2session）。
 */
public class WeChatMiniProgramClient implements WeChatSessionGateway {

    private final WeChatMiniProgramProperties properties;

    public WeChatMiniProgramClient(WeChatMiniProgramProperties properties) {
        this.properties = properties;
    }

    public WeChatMiniProgramProperties getProperties() {
        return properties;
    }

    @Override
    public String exchangeJsCode(String jsCode) {
        if (!properties.isConfigured()) {
            return "";
        }
        // TODO: WebClient GET jscode2sessionUrl ?appid=&secret=&js_code=&grant_type=authorization_code
        return "";
    }
}
