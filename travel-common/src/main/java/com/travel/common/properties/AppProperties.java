package com.travel.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * <p><b>作用：</b>绑定配置文件里以 {@code app} 为前缀的自定义项，与
 * {@code travel-bootstrap/src/main/resources/application.yml} 中的 {@code app:} 节一一对应。</p>
 * <p>Spring 在启动时把 YAML/环境变量注入到本类的字段；其它模块通过注入 {@code AppProperties} 读取 JWT、CORS 等，
 * 避免在代码里硬编码。</p>
 * <p><b>安全提示：</b>JWT 私钥路径只应指向服务器本地或密钥管理服务，切勿把私钥内容提交到 Git。</p>
 */

//第一次修改
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * 应用在监控、对外 {@code /info} 等场景展示的名称。
     */
    private String displayName = "travel-assistant";

    /**
     * 当前部署环境标识（如 dev / test / prod），可用于日志 MDC 或按环境切换行为。
     */
    private String environment = "dev";

    /**
     * JWT 相关：签发方、令牌有效期、类型 claim、RSA 密钥文件位置；由 user 模块签发与校验使用。
     */
    @NestedConfigurationProperty
    private final Jwt jwt = new Jwt();

    /**
     * Web 全局设置（目前主要是跨域 CORS），由 bootstrap 里的 MVC 配置类读取。
     */
    @NestedConfigurationProperty
    private final Web web = new Web();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Web getWeb() {
        return web;
    }

    /**
     * 对应 YAML 中的 {@code app.web}，内嵌 CORS 等子配置。
     */
    public static class Web {

        @NestedConfigurationProperty
        private final Cors cors = new Cors();

        public Cors getCors() {
            return cors;
        }

        /**
         * 跨域资源共享（CORS）参数；小程序或浏览器前端域名需出现在 {@code allowedOrigins} 中才能调 API。
         * 生产环境应避免 {@code *} 与过于宽松的配置。
         */
        public static class Cors {

            /** 是否启用 CORS 处理（false 时不应注册跨域相关 Bean） */
            private boolean enabled = false;

            /** 允许的 Origin 列表，如 https://servicewechat.com */
            private List<String> allowedOrigins = new ArrayList<>();

            /** 允许的 HTTP 方法 */
            private List<String> allowedMethods = new ArrayList<>(List.of(
                    "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            ));

            /** 允许的请求头；{@code *} 表示全部（需结合浏览器规范使用） */
            private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

            /** 是否允许携带 Cookie / Authorization 等凭证 */
            private boolean allowCredentials = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public List<String> getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(List<String> allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }

            public List<String> getAllowedMethods() {
                return allowedMethods;
            }

            public void setAllowedMethods(List<String> allowedMethods) {
                this.allowedMethods = allowedMethods;
            }

            public List<String> getAllowedHeaders() {
                return allowedHeaders;
            }

            public void setAllowedHeaders(List<String> allowedHeaders) {
                this.allowedHeaders = allowedHeaders;
            }

            public boolean isAllowCredentials() {
                return allowCredentials;
            }

            public void setAllowCredentials(boolean allowCredentials) {
                this.allowCredentials = allowCredentials;
            }
        }
    }

    /**
     * 对应 YAML {@code app.jwt}：RS256 双令牌（access / refresh）策略。
     */
    public static class Jwt {

        /**
         * JWT 标准 {@code iss} 声明：签发方标识，校验时必须一致。
         */
        private String issuer = "travel-assistant";

        /** access 令牌有效时长（短期，用于日常请求） */
        private Duration accessTokenTtl = Duration.ofMinutes(15);

        /** refresh 令牌有效时长（长期，仅用于换新 access） */
        private Duration refreshTokenTtl = Duration.ofDays(14);

        /**
         * 自定义 claim 名称：用于区分当前 JWT 是 access 还是 refresh（值见下面两个字段）。
         */
        private String tokenTypeClaimName = "token_typ";

        /** 当 {@code tokenTypeClaimName} 取该值时，表示 access 令牌 */
        private String accessTokenTypeValue = "access";

        /** 当 {@code tokenTypeClaimName} 取该值时，表示 refresh 令牌 */
        private String refreshTokenTypeValue = "refresh";

        /** PEM 文件路径（classpath 或 file:），私钥用于签名，公钥用于验签 */
        @NestedConfigurationProperty
        private final KeyLocations keys = new KeyLocations();

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }

        public String getTokenTypeClaimName() {
            return tokenTypeClaimName;
        }

        public void setTokenTypeClaimName(String tokenTypeClaimName) {
            this.tokenTypeClaimName = tokenTypeClaimName;
        }

        public String getAccessTokenTypeValue() {
            return accessTokenTypeValue;
        }

        public void setAccessTokenTypeValue(String accessTokenTypeValue) {
            this.accessTokenTypeValue = accessTokenTypeValue;
        }

        public String getRefreshTokenTypeValue() {
            return refreshTokenTypeValue;
        }

        public void setRefreshTokenTypeValue(String refreshTokenTypeValue) {
            this.refreshTokenTypeValue = refreshTokenTypeValue;
        }

        public KeyLocations getKeys() {
            return keys;
        }

        /**
         * 对应 {@code app.jwt.keys}：RSA 公私钥在磁盘或 classpath 上的位置。
         */
        public static class KeyLocations {

            /** PKCS#8 PEM 私钥路径（仅服务端可读） */
            private String privateLocation = "";

            /** SPKI PEM 公钥路径（可给网关或多实例共用验签） */
            private String publicLocation = "";

            public String getPrivateLocation() {
                return privateLocation;
            }

            public void setPrivateLocation(String privateLocation) {
                this.privateLocation = privateLocation;
            }

            public String getPublicLocation() {
                return publicLocation;
            }

            public void setPublicLocation(String publicLocation) {
                this.publicLocation = publicLocation;
            }
        }
    }
}
