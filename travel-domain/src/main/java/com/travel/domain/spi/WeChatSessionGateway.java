package com.travel.domain.spi;

/**
 * 微信小程序 jscode2session 网关 SPI，由 integration 模块实现。
 */
public interface WeChatSessionGateway {

    /**
     * @param jsCode 小程序 wx.login 临时 code
     * @return 业务侧可解析的会话摘要（占位：可封装 openid 等）
     */
    String exchangeJsCode(String jsCode);
}
