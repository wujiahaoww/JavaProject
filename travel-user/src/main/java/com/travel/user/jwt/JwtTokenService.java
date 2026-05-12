package com.travel.user.jwt;

import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.common.properties.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * <p><b>作用：</b>用户模块 JWT 核心服务：<strong>RS256</strong>（RFC 7518）——
 * 使用 {@link RSAPrivateKey} 对 JWT 签名，使用 {@link RSAPublicKey} 验签；与 JJWT 的 {@code Jwts.SIG.RS256} 对应。</p>
 * <ul>
 *   <li><b>签发</b>：{@link #issuePair(String)}，用于登录 / 注册并登录、刷新成功后。</li>
 *   <li><b>校验 access</b>：{@link #validateAccessToken(String)}，供过滤器走公钥验签 + iss + 类型 claim。</li>
 *   <li><b>刷新</b>：{@link #rotateFromRefresh(String)}，仅接受 refresh 类型，通过后旋转签发新的一对令牌。</li>
 * </ul>
 * <p>access 与 refresh 通过 {@link AppProperties.Jwt#getTokenTypeClaimName()} 与两类取值区分，禁止混用。</p>
 * <p>仅在容器中存在 {@link RSAPrivateKey} Bean 时注册（与 {@link com.travel.user.config.JwtKeyConfiguration} 一致）。</p>
 */
@Service
@ConditionalOnBean(RSAPrivateKey.class)
public class JwtTokenService {

    private final AppProperties appProperties;
    private final RSAPrivateKey signingKey;
    private final RSAPublicKey verificationKey;

    public JwtTokenService(AppProperties appProperties, RSAPrivateKey signingKey, RSAPublicKey verificationKey) {
        this.appProperties = appProperties;
        this.signingKey = signingKey;
        this.verificationKey = verificationKey;
    }

    /**
     * 为指定 {@code subject}（通常 openid 或内部 userId）签发一对新令牌。
     */
    public TokenPair issuePair(String subject) {
        Date now = new Date();
        var jwt = appProperties.getJwt();
        String typClaim = jwt.getTokenTypeClaimName();

        // —— access：短期，用于日常 API 的 Authorization 头 ——
        Date accessExp = new Date(now.getTime() + jwt.getAccessTokenTtl().toMillis());
        String access = Jwts.builder()
                .subject(subject)
                .issuer(jwt.getIssuer())
                .claim(typClaim, jwt.getAccessTokenTypeValue())
                .issuedAt(now)
                .expiration(accessExp)
                // RS256：RSA 私钥签名，Header 中 alg=RS256
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        // —— refresh：长期，仅用于调用 /auth/refresh ——
        Date refreshExp = new Date(now.getTime() + jwt.getRefreshTokenTtl().toMillis());
        String refresh = Jwts.builder()
                .subject(subject)
                .issuer(jwt.getIssuer())
                .claim(typClaim, jwt.getRefreshTokenTypeValue())
                .issuedAt(now)
                .expiration(refreshExp)
                // RS256：与 access 同一私钥，类型 claim 区分 refresh
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        // 返回给前端的过期秒数（至少 1，避免除零或歧义）
        long accessSec = Math.max(1, jwt.getAccessTokenTtl().getSeconds());
        long refreshSec = Math.max(1, jwt.getRefreshTokenTtl().getSeconds());
        return new TokenPair(access, refresh, accessSec, refreshSec, subject);
    }

    /**
     * <strong>刷新令牌</strong>：用公钥校验 refresh 的签名、{@code iss}、过期时间与类型 claim；
     * 通过后使用私钥为同一 {@code sub} 重新签发<strong>新的</strong> access 与 refresh（旋转刷新）。
     * <p>客户端拿到新令牌后应<strong>丢弃旧 refresh</strong>；旧 refresh 仍在过期时间前理论上可重复使用，
     * 若需服务端强制一次性刷新，可后续结合 Redis 记录 jti 黑名单扩展。</p>
     */
    public TokenPair rotateFromRefresh(String refreshToken) {
        Claims claims = parseSignedClaims(refreshToken, GlobalErrorCode.REFRESH_TOKEN_INVALID);
        assertTokenType(claims, appProperties.getJwt().getRefreshTokenTypeValue(), GlobalErrorCode.REFRESH_TOKEN_INVALID);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.REFRESH_TOKEN_INVALID);
        }
        return issuePair(subject);
    }

    /**
     * 供过滤器调用：验证 access 令牌并返回 subject。
     */
    public String validateAccessToken(String token) {
        Claims claims = parseSignedClaims(token, GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        assertTokenType(claims, appProperties.getJwt().getAccessTokenTypeValue(), GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new BusinessException(GlobalErrorCode.TOKEN_INVALID_OR_EXPIRED);
        }
        return subject;
    }

    /** 确保 JWT 上自定义「类型」claim 与期望一致（防止用 refresh 当 access 用） */
    private void assertTokenType(Claims claims, String expected, GlobalErrorCode errorCode) {
        Object v = claims.get(appProperties.getJwt().getTokenTypeClaimName());
        if (v == null || !expected.equals(String.valueOf(v))) {
            throw new BusinessException(errorCode);
        }
    }

    /** 公钥验签 + 校验 iss；过期或格式错误转为业务异常 */
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
