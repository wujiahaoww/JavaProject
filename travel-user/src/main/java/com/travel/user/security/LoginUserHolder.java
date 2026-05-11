package com.travel.user.security;

/**
 * <p><b>作用：</b>在同一次 HTTP 请求线程内保存「当前已认证用户」的 JWT {@code sub}（字符串）。</p>
 * <p>由 {@link JwtAuthenticationFilter} 在验签成功后 {@link #setSubject(String)}，
 * 业务代码通过 {@link #getSubject()} 读取；请求结束必须 {@link #clear()}，避免线程池复用导致数据泄漏。</p>
 */
public final class LoginUserHolder {

    /** 每个线程独立一份 subject，互不干扰 */
    private static final ThreadLocal<String> SUBJECT = new ThreadLocal<>();

    private LoginUserHolder() {
    }

    /** 写入当前请求的用户标识（仅应在过滤器中调用） */
    public static void setSubject(String subject) {
        SUBJECT.set(subject);
    }

    /** 读取当前请求的用户标识；未登录或未经过过滤器时为 {@code null} */
    public static String getSubject() {
        return SUBJECT.get();
    }

    /** 移除线程本地变量，必须在请求处理链路末尾调用 */
    public static void clear() {
        SUBJECT.remove();
    }
}
