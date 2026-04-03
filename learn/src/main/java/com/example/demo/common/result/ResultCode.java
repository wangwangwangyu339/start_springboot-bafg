package com.example.demo.common.result;

import lombok.Getter;

/**
 * 统一业务状态码枚举
 */
@Getter
public enum ResultCode {

    // 2xx 成功
    SUCCESS(200, "操作成功"),

    // 4xx 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    VALIDATION_FAILED(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),

    // 5xx 服务端错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    UPSTREAM_ERROR(502, "上游服务调用失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
