package com.zhao.seckill.service;

import com.zhao.seckill.vo.UserVo;
import com.zhao.seckill.vo.params.LoginParams;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 17:50
 * @description 登陆服务
 */
public interface ILoginService {

    /**
     * 登陆
     * @param loginParams
     * @return 登陆用户信息
     */
    UserVo login(LoginParams loginParams);

    ///**
    // * 查找token对应的用户信息（检查token合法性）
    // * @param token
    // * @return UserVo
    // */
    //UserVo checkToken(String token);

    /**
     * 查找token对应的用户信息（检查token合法性）
     * @param token
     * @return 用户ID
     */
    Long checkToken(String token);
}
