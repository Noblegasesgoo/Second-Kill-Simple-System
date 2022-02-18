package com.zhao.seckill.exception;

import com.zhao.seckill.common.enums.StatusCode;
import org.springframework.util.StringUtils;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 9:41
 * @description 断言配置类
 */
public class Assert {

    /**
     * 断言对象不为空
     * 如果对象obj为空，则抛出异常
     * @param obj 待判断对象
     */
    public static void notNull(Object obj, StatusCode statusCode) {

        if (obj == null) {
            throw new GlobalException(statusCode);
        }
    }

    /**
     * 断言对象为空
     * 如果对象obj不为空，则抛出异常
     * @param object
     * @param statusCode
     */
    public static void isNull(Object object, StatusCode statusCode) {

        if (object != null) {
            throw new GlobalException(statusCode);
        }
    }

    /**
     * 断言表达式为真
     * 如果不为真，则抛出异常
     * @param expression 是否成功
     */
    public static void isTrue(boolean expression, StatusCode statusCode) {

        if (!expression) {
            throw new GlobalException(statusCode);
        }
    }

    /**
     * 断言两个对象不相等
     * 如果相等，则抛出异常
     * @param m1
     * @param m2
     * @param statusCode
     */
    public static void notEquals(Object m1, Object m2, StatusCode statusCode) {

        if (m1.equals(m2)) {
            throw new GlobalException(statusCode);
        }
    }

    /**
     * 断言两个对象相等
     * 如果不相等，则抛出异常
     * @param m1
     * @param m2
     * @param statusCode
     */
    public static void equals(Object m1, Object m2, StatusCode statusCode) {

        if (!m1.equals(m2)) {
            throw new GlobalException(statusCode);
        }
    }

    /**
     * 断言参数不为空
     * 如果为空，则抛出异常
     * @param s
     * @param statusCode
     */
    public static void notEmpty(String s, StatusCode statusCode) {

        if (StringUtils.isEmpty(s)) {
            throw new GlobalException(statusCode);
        }
    }

}
