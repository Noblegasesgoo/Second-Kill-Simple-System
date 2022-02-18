package com.zhao.seckill.service.impl;

import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.domain.pojo.User;
import com.zhao.seckill.exception.GlobalException;
import com.zhao.seckill.service.ILoginService;
import com.zhao.seckill.service.IUserService;
import com.zhao.seckill.utils.JWTUtil;
import com.zhao.seckill.utils.MD5Util;
import com.zhao.seckill.vo.UserVo;
import com.zhao.seckill.vo.params.LoginParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 17:51
 * @description
 */

@Slf4j
@Service
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 登陆
     * @param loginParams
     * @return 登陆用户信息
     */
    @Override
    public UserVo login(LoginParams loginParams) {

        String mobile = loginParams.getMobile();
        String password = loginParams.getPassword();

        /** 重复性校验在注册中进行 此处默认数据库中无重复 **/

        /** 判断是否有该用户 **/
        User user = userService.listUserByIdForLogin(Long.valueOf(mobile));
        if (null == user) {
            throw new GlobalException(StatusCode.NOT_EXIST);
        }

        /** 判断密码是否正确 **/
        if (!MD5Util.inputPasswordToDBPassword(password).equals(user.getPassword())) {
            throw new GlobalException(StatusCode.PASSWORD_ERROR);
        }

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);

        /** 生成 token **/
        String token = JWTUtil.createToken(Long.valueOf(mobile));
        userVo.setToken(token);

        ///** 将 token 存入 redis 中 **/
        //redisTemplate.opsForValue().set("TOKEN_" + token
        //        , JSON.toJSONString(userVo)
        //        , 1
        //        , TimeUnit.DAYS);

        /** 设置**/
        user.setLastLoginDate(new Date());
        user.setLoginCount(user.getLoginCount()+1);
        userService.updateById(user);

        return userVo;
    }

    /**
     * 查找token对应的用户信息（检查token合法性）
     * @param token
     * @return 用户ID
     */
    @Override
    public Long checkToken(String token) {

        /** 检查 是否为空 **/
        if (StringUtils.isBlank(token)) {
            return null;
        }

        /** 检查 token密钥 **/
        if (null == JWTUtil.checkToken(token)) {
            return null;
        }


        Long userIdFromToken = JWTUtil.getUserIdFromToken(token);

        return userIdFromToken;
    }

    ///**
    // * 查找token对应的用户信息（检查token合法性）
    // * @param token
    // * @return UserVo
    // */
    //@Override
    //public UserVo checkToken(String token) {
    //
    //    /** 检查 是否为空 **/
    //    if (StringUtils.isBlank(token)) {
    //        return null;
    //    }
    //
    //    /** 检查 token密钥 **/
    //    if (null == JWTUtil.checkToken(token)) {
    //        return null;
    //    }
    //
    //    /** 检查 redis **/
    //    String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
    //    if (null == userJson) {
    //        return null;
    //    }
    //
    //    UserVo user = JSON.parseObject(userJson, UserVo.class);
    //
    //    return user;
    //}

}
