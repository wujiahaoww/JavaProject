package com.travel.user.jwt;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.common.properties.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * <p><b>作用：</b>用户模块 JWT 核心服务：<strong>RS256</strong>（RFC 7518）——
 * 私钥签发、公钥验签；与 JJWT 的 {@code Jwts.SIG.RS256} 对应。</p>
 * <ul>
 *   <li><b>签发</b>：{@link #issuePair(String)}；refresh 带标准 {@code jti}；Redis 以用户 {@code sub} 为 key、当前 {@code jti} 为 value，TTL 与 refresh 过期一致（同用户再次登录会覆盖旧 jti）。</li>
 *   <li><b>校验 access</b>：{@link #validateAccessToken(String)}，无 Redis 参与。</li>
 *   <li><b>刷新</b>：{@link #rotateFromRefresh(String)} — 验签后用 {@code sub} 查 Redis 中的 jti，与令牌 {@code jti} 一致则旋转签发新一对并更新 Redis；否则拒绝并提示重新登录。</li>
 * </ul>
 * <p>在存在 {@link RSAPrivateKey} Bean（由 {@link com.travel.user.config.JwtKeyConfiguration} 在配置了 PEM 路径后注册）时启用；
 * {@link StringRedisTemplate} 通过构造器注入，由 Spring 按依赖顺序解析，避免在类上使用
 * {@code @ConditionalOnBean(StringRedisTemplate)} 与 Redis 自动配置顺序冲突。</p>
 */
@Service
@ConditionalOnBean(RSAPrivateKey.class)
public class JwtTokenService {

    /** Redis 中 refresh 绑定：key = 前缀 + 用户 {@code sub}，value = 当前有效的 JWT {@code jti} */
    private static final String REDIS_REFRESH_USER_PREFIX = "travel:jwt:refresh:user:";

    private final AppProperties appProperties;
    private final RSAPrivateKey signingKey;
    private final RSAPublicKey verificationKey;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtTokenService(
            AppProperties appProperties,
            RSAPrivateKey signingKey,
            RSAPublicKey verificationKey,
            StringRedisTemplate stringRedisTemplate
    ) {
        this.appProperties = appProperties;
        this.signingKey = signingKey;
        this.verificationKey = verificationKey;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 为指定 {@code subject} 签发 access + refresh；refresh 写入标准 {@code jti}；Redis 以用户为 key 记录当前 jti。
     */
    public TokenPair issuePair(String subject) {
        String sub = subject == null ? "" : subject.trim();
        if (sub.isEmpty()) {
            throw new BusinessException(GlobalErrorCode.BAD_REQUEST, "签发令牌时 subject 不能为空");
        }
        Date now = new Date();
        var jwt = appProperties.getJwt();
        String typClaim = jwt.getTokenTypeClaimName();
        String refreshJti = UUID.randomUUID().toString().replace("-", "");

        Date accessExp = new Date(now.getTime() + jwt.getAccessTokenTtl().toMillis());
        String access = Jwts.builder()
                .subject(sub)
                .issuer(jwt.getIssuer())
                .claim(typClaim, jwt.getAccessTokenTypeValue())
                .issuedAt(now)
                .expiration(accessExp)
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        Date refreshExp = new Date(now.getTime() + jwt.getRefreshTokenTtl().toMillis());
        String refresh = Jwts.builder()
                .id(refreshJti)
                .subject(sub)
                .issuer(jwt.getIssuer())
                .claim(typClaim, jwt.getRefreshTokenTypeValue())
                .issuedAt(now)
                .expiration(refreshExp)
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        // Redis：用户 -> 当前有效 jti；TTL 与 refresh JWT 一致（再次登录会覆盖，使旧 refresh 失效）
        stringRedisTemplate.opsForValue().set(
                REDIS_REFRESH_USER_PREFIX + sub,
                refreshJti,
                jwt.getRefreshTokenTtl()
        );

        long accessSec = Math.max(1, jwt.getAccessTokenTtl().getSeconds());
        long refreshSec = Math.max(1, jwt.getRefreshTokenTtl().getSeconds());
        return new TokenPair(access, refresh, accessSec, refreshSec, sub);
    }

    /**
     * 刷新：验签 + 校验类型后，用用户 {@code sub} 从 Redis 取当前 jti，与令牌中 {@code jti} 一致则旋转签发新一对（并更新 Redis）；否则要求重新登录。
     */
    public TokenPair rotateFromRefresh(String refreshToken) {
        Claims claims = parseSignedClaims(refreshToken, GlobalErrorCode.REFRESH_TOKEN_INVALID);
        assertTokenType(claims, appProperties.getJwt().getRefreshTokenTypeValue(), GlobalErrorCode.REFRESH_TOKEN_INVALID);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.REFRESH_TOKEN_INVALID);
        }
        String sub = subject.trim();
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new BusinessException(GlobalErrorCode.REFRESH_TOKEN_INVALID, "刷新令牌缺少会话标识，请重新登录");
        }

        String redisKey = REDIS_REFRESH_USER_PREFIX + sub;
        String storedJti = stringRedisTemplate.opsForValue().get(redisKey);
        if (storedJti == null || storedJti.isBlank()) {
            throw new BusinessException(GlobalErrorCode.REFRESH_TOKEN_INVALID, "刷新令牌已失效，请重新登录");
        }
        if (!jti.trim().equals(storedJti.trim())) {
            throw new BusinessException(GlobalErrorCode.REFRESH_TOKEN_INVALID, "刷新令牌已失效，请重新登录");
        }

        // 与 issuePair 内 SET 形成旋转：新 refresh 对应新 jti；旧 refresh 因 jti 不再匹配而不可用
        return issuePair(sub);
    }

    public String validateAccessToken(String token) {
        Claims claims = parseSignedClaims(token, GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        assertTokenType(claims, appProperties.getJwt().getAccessTokenTypeValue(), GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        }
        return subject;
    }

    private void assertTokenType(Claims claims, String expected, GlobalErrorCode errorCode) {
        Object v = claims.get(appProperties.getJwt().getTokenTypeClaimName());
        if (v == null || !expected.equals(String.valueOf(v))) {
            throw new BusinessException(errorCode);
        }
    }

    private Claims parseSignedClaims(String token, GlobalErrorCode onFailure) {
        try {
            return Jwts.parser()
                    .verifyWith(verificationKey)
                    .requireIssuer(appProperties.getJwt().getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(onFailure, "令牌已过期");
        } catch (JwtException e) {
            throw new BusinessException(onFailure, "令牌无效");
        }
    }
}
