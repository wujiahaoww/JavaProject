package com.travel.config;

import com.travel.common.properties.AppProperties;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p><b>作用：</b>向 Spring Boot Actuator 的 {@code /actuator/info} 端点追加自定义段落，便于运维区分实例。</p>
 * <p>需在配置中暴露 {@code info} 端点（如 {@code management.endpoints.web.exposure.include}）后浏览器或脚本才可访问。</p>
 */
@Component
public class ApplicationInfoContributor implements InfoContributor {

    private final AppProperties appProperties;

    public ApplicationInfoContributor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 在 info JSON 中增加 {@code application} 节点，包含展示名与运行环境标识。
     */
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("displayName", appProperties.getDisplayName());
        app.put("environment", appProperties.getEnvironment());
        builder.withDetail("application", app);
    }
}
