package com.travel.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * <p><b>作用：</b>为每个 HTTP 请求分配或透传一个「请求 ID」，写入日志 MDC 并在响应头带回，方便排查问题、串联网关与多服务日志。</p>
 * <p>若客户端已在请求头带上 {@value #HEADER_NAME}，则沿用该值；否则服务端生成 UUID（去掉横线）。</p>
 */
public class RequestIdFilter extends OncePerRequestFilter {

    /** 与前端 / 网关约定的请求头名称 */
    public static final String HEADER_NAME = "X-Request-Id";

    /** 日志框架 MDC 中的键名，logback 等可通过 {@code %X{requestId}} 输出 */
    public static final String MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 优先使用调用方传入的追踪 ID，便于全链路对齐
        String id = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(id)) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
        // 同一线程内后续日志可带上 requestId
        MDC.put(MDC_KEY, id);
        // 响应头回传，客户端可保存用于反馈问题
        response.setHeader(HEADER_NAME, id);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 必须清理，避免线程池复用时串用上一个请求的 MDC
            MDC.remove(MDC_KEY);
        }
    }
}
