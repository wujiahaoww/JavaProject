package com.travel.config;

import com.travel.common.properties.AppProperties;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 向 Actuator {@code /actuator/info} 注入应用展示名与环境，便于运维与探活脚本识别实例。
 */
@Component
public class ApplicationInfoContributor implements InfoContributor {

    private final AppProperties appProperties;

    public ApplicationInfoContributor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("displayName", appProperties.getDisplayName());
        app.put("environment", appProperties.getEnvironment());
        builder.withDetail("application", app);
    }
}
