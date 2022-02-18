package com.zhao.seckill.vo.params;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhao.seckill.common.annotations.IsMobile;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/5 16:53
 * @description 登陆参数接收
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("goo_user")
public class LoginParams {

    @NotNull
    @IsMobile
    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @NotNull
    @ApiModelProperty(value = "密码")
    private String password;

}
