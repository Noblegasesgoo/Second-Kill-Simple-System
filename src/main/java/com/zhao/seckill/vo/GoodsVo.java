package com.zhao.seckill.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 14:39
 * @description goodsVo
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="goodsVo")
public class GoodsVo {

    @ApiModelProperty(value = "商品id")
    private Long id;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "商品标题")
    private String goodTitle;

    @ApiModelProperty(value = "商品图片")
    private String goodImg;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal goodsPrice;
}
