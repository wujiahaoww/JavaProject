package com.travel.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.properties.AppProperties;
import com.travel.user.api.AuthController;
import com.travel.user.jwt.JwtTokenService;
import com.travel.user.security.JwtAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p><b>作用：</b>当配置了 {@code app.jwt.keys.private-location} 时，从 PEM 文件加载 RSA 密钥对，
 * 并注册与 JWT 相关的 Bean：签名/验签密钥、刷新控制器、认证过滤器。</p>
 * <p>将「有密钥才暴露认证能力」集中在本配置类，避免应用在无密钥时仍注册无法工作的 {@link AuthController}。</p>
 * <p><b>生成密钥示例（2048 位，PKCS#8 私钥 + SPKI 公钥）：</b></p>
 * <pre>
 * openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
 * openssl pkey -in private.pem -pubout -out public.pem
 * </pre>
 */
@Configuration
@ConditionalOnProperty(prefix = "app.jwt.keys", name = "private-location")
public class JwtKeyConfiguration {

    private final AppProperties appProperties;
    private final ResourceLoader resourceLoader;

    public JwtKeyConfiguration(AppProperties appProperties, ResourceLoader resourceLoader) {
        this.appProperties = appProperties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * RSA 私钥 Bean：供 {@link JwtTokenService} 签发 JWT。
     */
    @Bean
    public RSAPrivateKey jwtSigningKey() throws IOException, GeneralSecurityException {
        String loc = appProperties.getJwt().getKeys().getPrivateLocation();
        if (loc == null || loc.isBlank()) {
            throw new IllegalStateException("app.jwt.keys.private-location is blank");
        }
        String pem = readPem(resourceLoader.getResource(loc));
        return parseRsaPrivateKeyPkcs8(pem);
    }

    /**
     * RSA 公钥 Bean：供 {@link JwtTokenService} 与网关验签。
     */
    @Bean
    public RSAPublicKey jwtVerificationKey() throws IOException, GeneralSecurityException {
        String loc = appProperties.getJwt().getKeys().getPublicLocation();
        if (loc == null || loc.isBlank()) {
            throw new IllegalStateException("app.jwt.keys.public-location is required when private key is set");
        }
        String pem = readPem(resourceLoader.getResource(loc));
        return parseRsaPublicKeySpki(pem);
    }

    /**
     * 手动注册 {@link AuthController}，保证仅在本配置生效时可用。
     */
    @Bean
    public AuthController authController(JwtTokenService jwtTokenService) {
        return new AuthController(jwtTokenService);
    }

    /**
     * 将 JWT 过滤器挂到 {@code /api/v1/*}，顺序略高于一般过滤器，便于尽早拒绝非法请求。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtTokenService jwtTokenService,
            ObjectMapper objectMapper
    ) {
        var registration = new FilterRegistrationBean<JwtAuthenticationFilter>();
        registration.setFilter(new JwtAuthenticationFilter(jwtTokenService, objectMapper));
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("jwtAuthenticationFilter");
        return registration;
    }

    /** 从 classpath 或文件系统读取 PEM 文本 */
    private static String readPem(Resource resource) throws IOException {
        if (!resource.exists()) {
            throw new IOException("Key resource not found: " + resource);
        }
        try (var in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** PKCS#8 「BEGIN PRIVATE KEY」→ {@link RSAPrivateKey} */
    private static RSAPrivateKey parseRsaPrivateKeyPkcs8(String pem) throws GeneralSecurityException {
        byte[] der = decodePemBlock(pem, "PRIVATE KEY");
        var spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /** SPKI 「BEGIN PUBLIC KEY」→ {@link RSAPublicKey} */
    private static RSAPublicKey parseRsaPublicKeySpki(String pem) throws GeneralSecurityException {
        byte[] der = decodePemBlock(pem, "PUBLIC KEY");
        var spec = new X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /** 剥离 PEM 头尾与空白，Base64 解码为 DER */
    private static byte[] decodePemBlock(String pem, String label) {
        String begin = "-----BEGIN " + label + "-----";
        String end = "-----END " + label + "-----";
        int s = pem.indexOf(begin);
        int e = pem.indexOf(end);
        if (s < 0 || e < 0) {
            throw new IllegalArgumentException("PEM must contain " + begin + " / " + end);
        }
        String b64 = pem.substring(s + begin.length(), e).replaceAll("\\s+", "");
        return Base64.getDecoder().decode(b64);
    }
}
