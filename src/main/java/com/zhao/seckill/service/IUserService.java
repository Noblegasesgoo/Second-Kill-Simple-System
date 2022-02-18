package com.zhao.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.seckill.domain.pojo.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-05
 */
public interface IUserService extends IService<User> {

    /**
     * 根据id查询对应用户信息
     * @param id
     * @return 用户信息
     */
    User listUserByIdForLogin(Long id);

    /**
     * 根据cookie中的ticket去redis中查找对应用户信息
     * @param request
     * @param response
     * @param ticket
     * @return 用户信息
     */
    User listUserByCookie(HttpServletRequest request, HttpServletResponse response, String ticket);
}
