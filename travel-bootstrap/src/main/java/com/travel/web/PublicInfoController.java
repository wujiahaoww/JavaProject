package com.travel.web;

import com.travel.common.api.ApiResult;
import com.travel.common.properties.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 无需登录的公开信息（名称、环境），供小程序启动时探测后端或展示「关于」。
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicInfoController {

    private final AppProperties appProperties;

    public PublicInfoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/info")
    public ApiResult<PublicAppInfo> info() {
        return ApiResult.ok(new PublicAppInfo(
                appProperties.getDisplayName(),
                appProperties.getEnvironment()
        ));
    }

    public record PublicAppInfo(String displayName, String environment) {
    }
}
