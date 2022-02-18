package com.zhao.seckill.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("goo_order")
@ApiModel(value="Order对象", description="")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "商品ID")
    @TableField("goods_id")
    private Long goodsId;

    @ApiModelProperty(value = "收获地址ID")
    @TableField("delivery_addr_id")
    private Long deliveryAddrId;

    @ApiModelProperty(value = "商品名字")
    @TableField("goods_name")
    private String goodsName;

    @ApiModelProperty(value = "商品数量")
    @TableField("goods_count")
    private Integer goodsCount;

    @ApiModelProperty(value = "商品价格")
    @TableField("goods_price")
    private BigDecimal goodsPrice;

    @ApiModelProperty(value = "1 pc,2 android, 3 ios")
    @TableField("order_channel")
    private Integer orderChannel;

    @ApiModelProperty(value = "订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退货，5已完成")
    private Integer status;

    @ApiModelProperty(value = "订单创建时间")
    @TableField("create_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createDate;

    @ApiModelProperty(value = "支付时间")
    @TableField("pay_date")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date payDate;


}
