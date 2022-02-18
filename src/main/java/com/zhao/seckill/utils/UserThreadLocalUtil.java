package com.zhao.seckill.utils;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 18:39
 * @description 本地线程内存空间存储用户信息工具类
 */

public class UserThreadLocalUtil {

    private UserThreadLocalUtil(){}

    private static final ThreadLocal<Long> LOCAL = new ThreadLocal<>();

    public static void put(Long userId){
        LOCAL.set(userId);
    }

    public static Long get(){
        return LOCAL.get();
    }

    public static void remove(){
        LOCAL.remove();
    }


    //private static final ThreadLocal<UserVo> LOCAL = new ThreadLocal<>();
    //
    //public static void put(UserVo userVo){
    //    LOCAL.set(userVo);
    //}
    //
    //public static UserVo get(){
    //    return LOCAL.get();
    //}
    //
    //public static void remove(){
    //    LOCAL.remove();
    //}
}
