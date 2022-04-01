package com.zhao.seckill.common.enums;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/1/19 11:34
 * @description 状态码
 */
public enum StatusCode {

    /** 客户端有关 **/
    STATUS_CODEC1(1, "test"),
    STATUS_CODEC200(200, "请求成功"),
    STATUS_CODEC_MINUS200(-200, "请求失败"),
    STATUS_CODEC400(400, "非法参数"),
    STATUS_CODEC401(401, "未登录，请先登陆后再尝试"),
    STATUS_CODEC403(403, "权限不足，请联系管理员"),
    STATUS_CODEC404(404, "找不到页面"),
    /** 服务器有关 **/
    STATUS_CODEC500(500, "服务器内部错误"),
    ABNORMAL_REQUEST(-100,"请求错误"),
    BAD_SQL_GRAMMAR_ERROR(-101, "sql语法错误"),
    SERVLET_ERROR(-102, "servlet请求异常"),
    UPLOAD_ERROR(-103, "文件上传错误"),

    /** 自定义类型有关 **/
    LOGIN_AUTH_ERROR(-201, "未登录"),
    PASSWORD_ERROR(-202,"密码错误"),
    PHONE_REPEAT(-203,"手机号已被注册"),
    NOT_EXIST(-204,"用户不存在"),
    EMPTY_STOCK(-205,"库存不足"),
    REPEAT_ORDER(-206,"重复下单"),
    FAIL_SECOND_KILL(-207,"秒杀失败"),
    REPEAT_REQ(-208,"重复请求"),
    VALID_ERROR(-209,"参数校验异常");

    /** 自定义状态码 **/
    private final Integer code;

    /** 自定义描述 **/
    private final String message;

    StatusCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
