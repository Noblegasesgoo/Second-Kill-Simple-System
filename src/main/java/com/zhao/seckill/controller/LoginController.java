package com.zhao.seckill.controller;

import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.controller.response.Response;
import com.zhao.seckill.service.ILoginService;
import com.zhao.seckill.vo.UserVo;
import com.zhao.seckill.vo.params.LoginParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 14:58
 * @description 登陆控制器
 */

@Slf4j
@RestController
@RequestMapping("")
@Api(tags = "登陆管理")
public class LoginController {

    @Resource
    private ILoginService loginService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Response login(@RequestBody @Valid LoginParams loginParams) {

        System.out.println(loginParams.getMobile());
        System.out.println(loginParams.getPassword());
        if (StringUtils.isBlank(loginParams.getMobile()) || StringUtils.isBlank(loginParams.getPassword())) {
            log.error("[seckill|LoginController|login] 参数不完整！");
            return Response.setResponse(StatusCode.STATUS_CODEC400).data(null);
        }

        UserVo user = loginService.login(loginParams);

        if (null == user) {
            log.error("[seckill|LoginController|login] 该用户未注册或密码错误！");
            return Response.setResponse(StatusCode.NOT_EXIST).data(null);
        }

        return Response.success().data(user);
    }

}
