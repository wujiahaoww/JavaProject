package com.travel.web;

import com.travel.common.api.ApiResult;
import com.travel.common.properties.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>作用：</b>提供<strong>无需登录</strong>的公开接口，供小程序启动时探测后端是否可用或展示「关于」信息。</p>
 * <p>路径在 JWT 过滤器白名单中（如 {@code /api/v1/public/**}），不要在此暴露敏感配置。</p>
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicInfoController {

    private final AppProperties appProperties;

    public PublicInfoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 返回应用展示名称与当前环境（来自 {@code app.display-name}、{@code app.environment}）。
     */
    @GetMapping("/info")
    public ApiResult<PublicAppInfo> info() {
        return ApiResult.ok(new PublicAppInfo(
                appProperties.getDisplayName(),
                appProperties.getEnvironment()
        ));
    }

    /**
     * 对外暴露的最小应用信息 DTO。
     *
     * @param displayName 应用对外展示名
     * @param environment 环境标识（如 dev / prod）
     */
    public record PublicAppInfo(String displayName, String environment) {
    }
}
