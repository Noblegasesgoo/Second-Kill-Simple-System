package com.zhao.seckill.service.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhao.seckill.domain.pojo.Goods;
import com.zhao.seckill.domain.pojo.Order;
import com.zhao.seckill.domain.pojo.SeckillGoods;
import com.zhao.seckill.domain.pojo.SeckillOrder;
import com.zhao.seckill.service.IGoodsService;
import com.zhao.seckill.service.IOrderService;
import com.zhao.seckill.service.ISeckillGoodsService;
import com.zhao.seckill.service.ISeckillOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/18 19:52
 * @description 秒杀系统消息消费者
 */

@Service
@Slf4j
public class SecondKillReceiver {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    /**
     * 异步更新商品信息消息
     * @param goodsId
     */
    @RabbitListener(queues = "goodsQueue")
    public void doUpdateStock(String goodsId){

        /** 这里不使用缓存中获取库存的方法是因为，缓存可能过期，取不到所需要的数了 **/
        /** 下面这样的库存减少方式是原子性的，是线程安全的 **/
        goodsService.update(new UpdateWrapper<Goods>().eq("id", Long.parseLong(goodsId))
                .setSql("stock_count=stock_count-1"));
        seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().eq("goods_id", Long.parseLong(goodsId))
                .setSql("stock_count=stock_count-1"));
    }

    /**
     * 异步更新订单消息
     * @param orderString
     */
    @RabbitListener(queues = "orderQueue")
    public void doUpdateOrder(String orderString){

        /** 将JSON字符串转化为对象 **/
        Order order = JSON.parseObject(orderString, Order.class);

        /** 执行订单表的数据库入库操作 **/
        orderService.save(order);

        /**执行秒杀订单表的数据库入库操作**/
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(order.getUserId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(order.getGoodsId());
        seckillOrderService.save(seckillOrder);
    }

}
