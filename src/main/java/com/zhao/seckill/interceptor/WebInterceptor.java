package com.zhao.seckill.interceptor;

import com.alibaba.fastjson.JSON;
import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.controller.response.Response;
import com.zhao.seckill.service.ILoginService;
import com.zhao.seckill.utils.UserThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 12:06
 * @description 登陆拦截器
 */

@Slf4j
@Component
public class WebInterceptor implements HandlerInterceptor {

    @Autowired
    private ILoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /** 放行controller无关的请求 **/
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        /** 检查token是否为空 **/
        if (StringUtils.isBlank(token)) {

            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(Response.setResponse(StatusCode.STATUS_CODEC401)));

            return false;
        }

        // UserVo userVo = loginService.checkToken(token);
        Long userId = loginService.checkToken(token);


        /** 进一步检查token是否过期 **/
        if (null == userId) {

            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(Response.setResponse(StatusCode.STATUS_CODEC401).message("登陆过期，请重新登陆!")));

            return false;
        }
        //if (null == userVo) {
        //
        //    response.setContentType("application/json;charset=utf-8");
        //    response.getWriter().print(JSON.toJSONString(Response.setResponse(StatusCode.STATUS_CODEC401).message("登陆过期，请重新登陆!")));
        //
        //    return false;
        //}

        /** 放入本地线程池 **/
        UserThreadLocalUtil.put(userId);
        //UserThreadLocalUtil.put(userVo);


        /** token合法 **/
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        /** 请求结束后释放空间 **/
        UserThreadLocalUtil.remove();
    }
}
