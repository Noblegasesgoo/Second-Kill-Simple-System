package com.zhao.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.seckill.common.enums.StatusCode;
import com.zhao.seckill.domain.pojo.Order;
import com.zhao.seckill.domain.pojo.SeckillOrder;
import com.zhao.seckill.exception.Assert;
import com.zhao.seckill.mapper.SeckillOrderMapper;
import com.zhao.seckill.service.ISeckillOrderService;
import com.zhao.seckill.service.mq.SecondKillSender;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private SecondKillSender secondKillSender;

    /**
     * 查询当前用户是否重复下单
     * @param userId
     * @return 是否重复下单
     */
    @Override
    public Boolean checkCurrentUserOrder(Long userId) {

        LambdaQueryWrapper<SeckillOrder> seckillOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        seckillOrderLambdaQueryWrapper.eq(SeckillOrder::getUserId, userId);
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(seckillOrderLambdaQueryWrapper);

        /** 断言他没有重复下单？断言表达式是否为真？重复下单的话就抛出异常，否则就正常执行 **/
        Assert.isTrue(null == seckillOrder, StatusCode.REPEAT_ORDER);

        return false;
    }

    /**
     * 秒杀下单
     * @param userId
     * @param goods
     * @return 秒杀订单
     */
    @Override
    public Order secondKill(Long userId, SeckillGoodsDetailVo goods) {

        /** 进入到该方法就代表着，抢到秒杀名额了所以我们直接进行订单创建操作 **/
        /** 将该订单预入 redis **/
        preOrder(userId, goods.getId());

        /** 生成普通订单 **/
        Order order = new Order();
        order.setUserId(userId);
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(goods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());

        /** mq异步保证数据最终一致性问题 **/
        secondKillSender.toDoUpdateOrder(order);

        /** 将抢购完成的订单信息返回 **/
        return order;
    }

    /**
     * 订单预入操作
     * @param userId
     * @param goodsId
     */
    private void preOrder(Long userId, Long goodsId) {

        String key = "SECONDKILL::USERID:" + userId + "-ORDER::GOODSID:" + goodsId;
        /** 缓存有效时间要比秒杀持续时间长 **/
        redisTemplate.opsForValue().set(key, "预入订单启用", 1, TimeUnit.DAYS);
    }

    /**
     * 查询当前用户是否重复下单
     * @param userId
     * @param goodsId
     * @return 是否重复下单
     */
    @Override
    public Boolean checkCurrentUserOrder(Long userId, Long goodsId) {

        String key = "SECONDKILL::USERID:" + userId + "-ORDER::GOODSID:" + goodsId;
        /** 断言他重复下单！断言表达式是否为真？重复下单的话就抛出异常，否则就正常执行 **/
        Assert.isTrue(StringUtils.isEmpty(redisTemplate.opsForValue().get(key)), StatusCode.REPEAT_ORDER);

        return false;
    }


}
