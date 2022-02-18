package com.zhao.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.seckill.domain.pojo.Order;
import com.zhao.seckill.domain.pojo.SeckillOrder;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    /**
     * 查询当前用户是否重复下单
     * @param userId
     * @return 是否重复下单
     */
    Boolean checkCurrentUserOrder(Long userId);

    /**
     * 查询当前用户是否重复下单
     * @param userId
     * @param goodsId
     * @return 是否重复下单
     */
    Boolean checkCurrentUserOrder(Long userId, Long goodsId);

    /**
     * 秒杀下单
     * @param userId
     * @param goods
     * @return 秒杀订单
     */
    Order secondKill(Long userId, SeckillGoodsDetailVo goods);

}
