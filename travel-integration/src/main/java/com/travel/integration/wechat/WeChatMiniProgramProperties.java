package com.travel.integration.wechat;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序服务端配置（预留：jscode2session、消息推送等）。
 * <p>
 * 敏感项通过环境变量注入，勿在仓库中填写真实 secret。
 * </p>
 */
@ConfigurationProperties(prefix = "app.wechat.mini-program")
public class WeChatMiniProgramProperties {

    /**
     * AppID（env: WECHAT_MINI_APP_ID）
     */
    private String appId = "";

    /**
     * AppSecret（env: WECHAT_MINI_APP_SECRET）
     */
    private String appSecret = "";

    /**
     * 登录 code 换 session 的官方地址，一般无需修改。
     */
    private String jscode2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getJscode2sessionUrl() {
        return jscode2sessionUrl;
    }

    public void setJscode2sessionUrl(String jscode2sessionUrl) {
        this.jscode2sessionUrl = jscode2sessionUrl;
    }

    public boolean isConfigured() {
        return appId != null && !appId.isBlank()
                && appSecret != null && !appSecret.isBlank();
    }
}
