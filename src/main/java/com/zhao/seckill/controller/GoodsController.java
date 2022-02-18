package com.zhao.seckill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhao.seckill.controller.response.Response;
import com.zhao.seckill.domain.pojo.Goods;
import com.zhao.seckill.service.IGoodsService;
import com.zhao.seckill.service.ISeckillGoodsService;
import com.zhao.seckill.utils.MD5Util;
import com.zhao.seckill.vo.GoodsDetailVo;
import com.zhao.seckill.vo.GoodsVo;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;
import com.zhao.seckill.vo.params.PageParams;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/13 12:29
 * @description 商品请求处理器
 */

@RestController
@RequestMapping("/goods")
@Api(tags = "商品管理")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @ApiOperation(value = "查询正常售卖商品列表请求" )
    @PostMapping("/public/list")
    public Response list(@RequestBody PageParams pageParams) {

        Page<GoodsVo> goodListVoPage = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<GoodsVo> result = goodsService.list(goodListVoPage);

        return Response.success().data(result);
    }


    /**
     * Windows 优化前QPS 150000: 1282.2
     * Linux 优化前QPS 150000：1891.4
     *
     *
     * @param goodId
     * @return
     */
    @ApiOperation(value = "查询正常售卖商品详情请求" )
    @GetMapping("/public/detail/{goodId}")
    public Response detail(@ApiParam(value = "商品id", required = true) @PathVariable Integer goodId) {

        Goods good = goodsService.getById(goodId);
        GoodsDetailVo goodDetailVo = new GoodsDetailVo();
        BeanUtils.copyProperties(good,goodDetailVo);

        System.out.println(MD5Util.inputPasswordToDBPassword("123456"));
        return Response.success().data(goodDetailVo);
    }

    @ApiOperation(value = "查询秒杀商品列表请求" )
    @PostMapping("/public/seckill/list")
    public Response seckillList(@RequestBody PageParams pageParams) {

        Page<SeckillGoodsDetailVo> seckillGoodDetailVoPage = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<SeckillGoodsDetailVo> result = seckillGoodsService.list(seckillGoodDetailVoPage);

        return Response.success().data(result);
    }

    @ApiOperation(value = "查询秒杀商品详情请求" )
    @GetMapping("/public/seckill/{id}")
    public Response seckillDetail(@ApiParam(value = "商品id", required = true) @PathVariable Long id) {

        SeckillGoodsDetailVo seckillGood = seckillGoodsService.ListById(id);
        return Response.success().data(seckillGood);
    }

}
