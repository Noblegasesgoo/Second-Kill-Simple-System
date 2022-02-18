package com.zhao.seckill.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 11:45
 * @description uservo
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="uservo")
public class UserVo {

    @ApiModelProperty(value = "用户ID，手机号码")
    private Long id;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String face;

    @ApiModelProperty(value = "登陆次数")
    private Integer loginCount;

    @ApiModelProperty(value = "是否禁用")
    private Integer isDeleted;

    @ApiModelProperty(value = "最后一次登陆时间")
    @TableField("last_login_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastLoginDate;

    @ApiModelProperty(value = "JWT访问令牌（登录后随请求头返回验证权限）")
    private String token;

}
