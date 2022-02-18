package com.zhao.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.seckill.domain.pojo.User;
import com.zhao.seckill.mapper.UserMapper;
import com.zhao.seckill.service.IUserService;
import com.zhao.seckill.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据id查询对应用户信息
     * @param id
     * @return 用户信息
     */
    @Override
    public User listUserByIdForLogin(Long id) {

        User user = userMapper.selectById(id);
        return user;
    }

    /**
     * 根据cookie中的ticket去redis中查找对应用户信息
     *
     * @param request
     * @param response
     * @param ticket
     * @return 用户信息
     */
    @Override
    public User listUserByCookie(HttpServletRequest request, HttpServletResponse response, String ticket) {

        if (StringUtils.isEmpty(ticket)) {
            return null;
        }

        User user = (User) redisTemplate.opsForValue().get("user:" + ticket);

        if (null != user) {
            CookieUtil.setCookie(request, response, "userTicket", ticket);
        }

        return user;
    }
}
