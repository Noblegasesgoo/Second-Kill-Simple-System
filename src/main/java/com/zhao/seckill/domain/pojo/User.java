package com.zhao.seckill.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("goo_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID，手机号码")
    private Long id;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "MD5(MD5(pass明文+固定salt) + salt)")
    private String password;

    @ApiModelProperty(value = "加密盐")
    private String salt;

    @ApiModelProperty(value = "头像")
    private String face;

    @ApiModelProperty(value = "登陆次数")
    @TableField("login_count")
    private Integer loginCount;

    @ApiModelProperty(value = "是否禁用")
    @TableField("is_deleted")
    private Integer isDeleted;

    @ApiModelProperty(value = "注册时间")
    @TableField("register_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date registerDate;

    @ApiModelProperty(value = "最后一次登陆时间")
    @TableField("last_login_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date lastLoginDate;

    @ApiModelProperty(value = "记录创建时间")
    @TableField("gmt_create")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date gmtCreate;

    @ApiModelProperty(value = "记录最后更新时间")
    @TableField("gmt_modified")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date gmtModified;

}
