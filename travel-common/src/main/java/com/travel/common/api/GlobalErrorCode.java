package com.travel.common.api;

/**
 * <p><b>作用：</b>全项目统一的「业务错误码 + 默认中文说明」枚举。</p>
 * <p>控制器或业务层抛出 {@link com.travel.common.exception.BusinessException} 时携带本枚举，
 * 全局异常处理器再转成 {@link ApiResult} 与合适的 HTTP 状态码（见 {@link #getSuggestedHttpStatus()}）。</p>
 */
public enum GlobalErrorCode {

    /** 成功：与 {@link ApiResult#ok()} 中使用的 code 一致 */
    SUCCESS(0, "OK"),

    /** 请求参数不合法、格式错误等 */
    BAD_REQUEST(40000, "参数错误"),

    /** 微信 code 换会话失败或未配置等 */
    WECHAT_LOGIN_FAILED(40001, "微信登录失败，请检查小程序配置与 jsCode"),

    /** 未带 Token 或 Token 无效（通用） */
    UNAUTHORIZED(40100, "未登录或令牌无效"),

    /** access 令牌过期、签名不对、类型不对等 */
    TOKEN_INVALID_OR_EXPIRED(40101, "访问令牌无效或已过期"),

    /** refresh 令牌不合法或过期 */
    REFRESH_TOKEN_INVALID(40102, "刷新令牌无效或已过期"),

    /** 已登录但无权限访问该资源 */
    FORBIDDEN(40300, "无权限"),

    /** 资源不存在 */
    NOT_FOUND(40400, "资源不存在"),

    /** 触发限流等 */
    TOO_MANY_REQUESTS(42900, "请求过于频繁"),

    /** 验证码发送过于频繁（单号码冷却） */
    OTP_SEND_COOLDOWN(40010, "发送过于频繁，请稍后再试"),

    /** 同一联系方式当日发送次数超限 */
    OTP_SEND_LIMIT_CONTACT(40011, "该号码或邮箱今日获取验证码次数已达上限"),

    /** 同一 IP 短时发送次数超限 */
    OTP_SEND_LIMIT_IP(40012, "当前网络请求过于频繁，请稍后再试"),

    /** 验证码连续错误达到上限，账户校验已暂时锁定 */
    OTP_LOCKED(40013, "验证码错误次数过多，请稍后再试"),

    /** 验证码错误或已失效 */
    OTP_INVALID(40014, "验证码错误或已过期"),

    /** 账号或密码错误（不区分具体原因，防枚举） */
    INVALID_CREDENTIALS(40103, "账号或密码错误"),

    /** 用户不存在（登录等场景） */
    USER_NOT_FOUND(40401, "用户不存在"),

    /** 手机号或邮箱已被注册 */
    USER_ALREADY_EXISTS(40901, "该手机号或邮箱已注册"),

    /** 未预期的服务端错误 */
    INTERNAL_ERROR(50000, "系统繁忙，请稍后重试"),

    /** 未配置 RSA 私钥等导致无法签发 JWT */
    JWT_NOT_CONFIGURED(50301, "服务端未配置 JWT 签名密钥"),

    /** 未启用 Redis，无法使用验证码能力 */
    REDIS_NOT_AVAILABLE(50302, "服务端未启用缓存，无法发送验证码");

    /** 返回给前端的数字错误码 */
    private final int code;

    /** 默认错误描述（可被 {@link com.travel.common.exception.BusinessException} 的自定义文案覆盖） */
    private final String message;

    GlobalErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 将业务错误映射为 HTTP 状态码，供 {@code GlobalExceptionHandler} 设置 {@code ResponseEntity.status(...)}。
     * 例如：401xx 系列对应 HTTP 401，便于网关与前端统一处理。
     */
    public int getSuggestedHttpStatus() {
        return switch (this) {
            case UNAUTHORIZED, TOKEN_INVALID_OR_EXPIRED, REFRESH_TOKEN_INVALID, INVALID_CREDENTIALS -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND, USER_NOT_FOUND -> 404;
            case USER_ALREADY_EXISTS -> 409;
            case TOO_MANY_REQUESTS, OTP_LOCKED, OTP_SEND_COOLDOWN, OTP_SEND_LIMIT_CONTACT, OTP_SEND_LIMIT_IP -> 429;
            case INTERNAL_ERROR -> 500;
            case JWT_NOT_CONFIGURED, REDIS_NOT_AVAILABLE -> 503;
            case SUCCESS -> 200;
            default -> 400;
        };
    }
}
