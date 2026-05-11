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
 * <p><b>作用：</b>JWT 的核心服务：用 RSA 私钥签发、用公钥校验；access 与 refresh 通过配置中的
 * {@link AppProperties.Jwt#getTokenTypeClaimName()} 声明区分，算法固定为 RS256。</p>
 * <p>仅在 Spring 容器中存在 {@link RSAPrivateKey} Bean 时注册（与密钥配置一致），避免无密钥时误用。</p>
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
                .signWith(signingKey, Jwts.SIG.RS256)
                .compact();

        // 返回给前端的过期秒数（至少 1，避免除零或歧义）
        long accessSec = Math.max(1, jwt.getAccessTokenTtl().getSeconds());
        long refreshSec = Math.max(1, jwt.getRefreshTokenTtl().getSeconds());
        return new TokenPair(access, refresh, accessSec, refreshSec, subject);
    }

    /**
     * 校验 refresh 签名与类型后，为同一 {@code sub} 重新签发一对令牌（旧 refresh 即作废语义由客户端丢弃旧令牌实现）。
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
