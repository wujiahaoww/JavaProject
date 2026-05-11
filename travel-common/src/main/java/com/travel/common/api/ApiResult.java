package com.travel.common.api;

/**
 * <p><b>作用：</b>统一 HTTP 接口返回给前端（或小程序）的 JSON 结构，避免每个接口各写一套字段名。</p>
 * <p><b>典型 JSON 形态：</b>{@code { "code": 0, "message": "OK", "data": { ... } }}</p>
 * <ul>
 *   <li>{@code code}：业务状态码，0 表示成功，非 0 表示失败（与 {@link GlobalErrorCode} 对应）。</li>
 *   <li>{@code message}：给人看的说明文字。</li>
 *   <li>{@code data}：成功时的具体数据；失败时多为 {@code null}。</li>
 * </ul>
 *
 * @param <T> {@code data} 字段的数据类型（如用户信息、列表等）
 */
public class ApiResult<T> {

    /** 业务状态码：0 成功，其它见 {@link GlobalErrorCode} */
    private int code;

    /** 提示信息，成功或失败都建议有简短说明 */
    private String message;

    /** 业务数据载荷；无数据时可 {@code null} */
    private T data;

    /** 无参构造：供 Jackson 等反序列化使用 */
    public ApiResult() {
    }

    /**
     * 全参构造：手动组装响应时使用。
     *
     * @param code    业务码
     * @param message 提示文案
     * @param data    数据体
     */
    public ApiResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功且无 {@code data}（例如只需告知「操作成功」）。
     */
    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(GlobalErrorCode.SUCCESS.getCode(), GlobalErrorCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功并携带 {@code data}。
     *
     * @param data 要返回给调用方的对象
     */
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(GlobalErrorCode.SUCCESS.getCode(), GlobalErrorCode.SUCCESS.getMessage(), data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
