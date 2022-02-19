package com.zhao.seckill.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.controller.response.Response;
import com.zhao.seckill.domain.pojo.Order;
import com.zhao.seckill.service.ISeckillGoodsService;
import com.zhao.seckill.service.ISeckillOrderService;
import com.zhao.seckill.utils.UserThreadLocalUtil;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/14 18:19
 * @description
 */

@RestController
@RequestMapping("/seckill")
@Api(tags = "秒杀管理")
public class SeckillController {

    @Resource
    private ISeckillGoodsService seckillGoodsService;

    @Resource
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "进行秒杀商品的库存缓存内更新请求" )
    @PostMapping("/public/update/cache")
    public Response updateCache() {

        IPage<SeckillGoodsDetailVo> list = seckillGoodsService.list(new Page<>(0, 10));
        List<SeckillGoodsDetailVo> records = list.getRecords();
        records.stream().forEach((record) -> {
            /** 设置秒杀商品缓存，一般情况下，缓存存在时间要大于秒杀总时间 **/
            redisTemplate.opsForValue().set( "SECONDKILL::PRODUCT:" + record.getId() + "-STOCK::COUNT", record.getStockCount(), 1, TimeUnit.DAYS);

            /** 设置内存标记 **/
            Map<String, Boolean> stockLocalOverMap = seckillGoodsService.getStockLocalOverMap();
            stockLocalOverMap.put(record.getId().toString(), record.getStockCount() > 0);
        });

        return Response.success().message("秒杀商品库存缓存更新成功");
    }


    @ApiOperation(value = "进行秒杀请求" )
    @PostMapping("/private/do")
    public Response doSecondKill(@ApiParam(value = "商品vo", required = true) @RequestBody SeckillGoodsDetailVo goods) {

        /** 得到当前登陆用户的信息 **/
        Long userId = UserThreadLocalUtil.get();

        /** 优先判断是否重复购买 **/
        Boolean isRepeat = seckillOrderService.checkCurrentUserOrder(userId, goods.getId());
        if (isRepeat) {
            return Response.setResponse(StatusCode.REPEAT_ORDER);
        }

        /** 检查库存以及预减库存 **/
        Map<String, Boolean> stockLocalOverMap = seckillGoodsService.getStockLocalOverMap();
        if (!stockLocalOverMap.get(goods.getId().toString())) {
            return Response.error().message("秒杀结束！");
        }

        Boolean hasStock =  seckillGoodsService.checkStockAndDecrement(goods.getId());
        if (!hasStock) {
            return Response.error().message("秒杀结束！");
        }

        /** 秒杀成功，下单 **/
        Order order = seckillOrderService.secondKill(userId, goods);

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("order", order);

        return Response.success().message("秒杀成功").data(data);
    }

}
