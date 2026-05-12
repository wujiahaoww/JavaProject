package com.travel.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.api.ApiResult;
import com.travel.common.api.GlobalErrorCode;
import com.travel.common.exception.BusinessException;
import com.travel.user.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p><b>作用：</b>保护以 {@code /api/v1/} 开头的 API：除白名单外，要求请求头携带
 * {@code Authorization: Bearer &lt;access_token&gt;}，并用公钥校验 JWT。</p>
 * <p>校验成功后把 {@code sub} 写入 {@link LoginUserHolder}，供控制器读取当前用户；
 * 请求结束在 {@code finally} 中清理 ThreadLocal，防止线程池复用导致串号。</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 无需 access 令牌的路径（登录、刷新、公开接口等）；Ant 风格，支持 {@code **}。
     */
    private final List<String> whitelistPatterns = List.of(
            "/api/v1/users/login/wechat",
            "/api/v1/users/register/wechat",
            "/api/v1/users/register/credential",
            "/api/v1/users/login/password",
            "/api/v1/users/login/code",
            "/api/v1/auth/otp/send",
            "/api/v1/auth/refresh",
            "/api/v1/public/**"
    );

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.objectMapper = objectMapper;
    }

    /**
     * 非 API 前缀或命中白名单的请求：本过滤器不处理，直接交给后续链。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/")) {
            return true;
        }
        for (String pattern : whitelistPatterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            // 必须是 Bearer 前缀（大小写不敏感比较前缀长度内字符）
            if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                writeUnauthorized(response, GlobalErrorCode.UNAUTHORIZED.getCode(), "缺少 Bearer 访问令牌");
                return;
            }
            String token = header.substring(BEARER_PREFIX.length()).trim();
            if (token.isEmpty()) {
                writeUnauthorized(response, GlobalErrorCode.UNAUTHORIZED.getCode(), "缺少 Bearer 访问令牌");
                return;
            }
            try {
                String subject = jwtTokenService.validateAccessToken(token);
                LoginUserHolder.setSubject(subject);
                filterChain.doFilter(request, response);
            } catch (BusinessException ex) {
                // 令牌过期、类型错误等：返回 JSON 体中的业务码，HTTP 401
                writeUnauthorized(response, ex.getCode(), ex.getMessage());
            }
        } finally {
            LoginUserHolder.clear();
        }
    }

    /** 统一未授权响应格式，与全局异常处理中的 {@link ApiResult} 结构一致 */
    private void writeUnauthorized(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResult<Void> body = new ApiResult<>();
        body.setCode(code);
        body.setMessage(message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
