package com.zhao.seckill.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 13:35
 * @description MD5 工具类
 */

@Component
public class MD5Util {

    /** 第一次前端明文密码输入传到后端的密码，该盐与前台统一 **/
    private static final String SALT = "ZlmWl1314@#*&^!@";

    /**
     * 加密方法
     * @param str
     * @return 加密后结果
     */
    public static String encode(String str) {

        return DigestUtils.md5Hex(str);
    }

    /**
     * 第一次加密
     * @param inputPassword
     * @return 第一次加密结果
     */
    public static String inputPasswordToFromPassword(String inputPassword) {

        /** 为了安全性，不用完整的盐 **/
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(SALT.charAt(2))
                .append(SALT.charAt(3))
                .append(inputPassword)
                .append(SALT.charAt(5))
                .append(SALT.charAt(6))
                .append(SALT.charAt(7))
                .append(SALT.charAt(8))
                .append(SALT.charAt(10))
                .append(SALT.charAt(12))
                .append(SALT.charAt(13));

        return encode(stringBuffer.toString());
    }

    /**
     * 第二次加密
     * @param fromPassword
     * @return 第二次加密结果
     */
    public static String formPasswordToDBPassword(String fromPassword) {
        /** 为了安全性，不用完整的盐 **/
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(SALT.charAt(2))
                .append(SALT.charAt(3))
                .append(fromPassword)
                .append(SALT.charAt(4))
                .append(SALT.charAt(6))
                .append(SALT.charAt(7))
                .append(SALT.charAt(9))
                .append(SALT.charAt(10))
                .append(SALT.charAt(10))
                .append(SALT.charAt(1));

        return encode(stringBuffer.toString());
    }

    /**
     * 实际调用方法
     * @param inputPassword
     * @param salt
     * @return 加密后的DB中存储的密码
     */
    public static String inputPasswordToDBPassword(String inputPassword) {

        String fromPassword = inputPasswordToFromPassword(inputPassword);
        String dbPassword = formPasswordToDBPassword(fromPassword);
        return dbPassword;
    }

}
