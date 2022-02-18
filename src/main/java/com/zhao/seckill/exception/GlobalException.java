package com.zhao.seckill.exception;

import com.zhao.seckill.common.enums.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/12 17:33
 * @description 全局异常
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException{

    //错误码
    private Integer code;
    //错误信息
    private String message;

    /**
     * @param message 错误消息
     */
    public GlobalException(String message) {
        this.message = message;
    }

    /**
     * @param message 错误消息
     * @param code 错误码
     */
    public GlobalException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    /**
     * @param message 错误消息
     * @param code 错误码
     * @param cause 原始异常对象
     */
    public GlobalException(String message, Integer code, Throwable cause) {
        super(cause);
        this.message = message;
        this.code = code;
    }

    /**
     * @param statusCode 接收枚举类型
     */
    public GlobalException(StatusCode statusCode) {
        this.message = statusCode.getMessage();
        this.code = statusCode.getCode();
    }

    /**
     * @param statusCode 接收枚举类型
     * @param cause 原始异常对象
     */
    public GlobalException(StatusCode statusCode, Throwable cause) {

        super(cause);
        this.message = statusCode.getMessage();
        this.code = statusCode.getCode();
    }
}
