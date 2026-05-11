package com.travel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 全局应用配置占位（后续扩展 JWT、微信、第三方 URL 等）。
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 对外展示名称。
     */
    private String displayName = "travel-assistant";

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
