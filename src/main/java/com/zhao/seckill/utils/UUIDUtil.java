package com.zhao.seckill.utils;

import java.util.UUID;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/13 12:11
 * @description UUID工具类
 */
public class UUIDUtil {

    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
