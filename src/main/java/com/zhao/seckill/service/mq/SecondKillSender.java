package com.zhao.seckill.service.mq;

import com.alibaba.fastjson.JSON;
import com.zhao.seckill.domain.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/18 19:52
 * @description 秒杀系统消息生产者
 */

@Service
@Slf4j
public class SecondKillSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 异步更新库存消息（数据库）
     * @param goodsId
     */
    public void toDoUpdateStock(Long goodsId) {
        rabbitTemplate.convertAndSend("seckillExchange", "secondkill.stock", goodsId.toString());
    }

    /**
     * 异步更新订单消息（数据库）
     * @param order
     */
    public void toDoUpdateOrder(Order order) {
        rabbitTemplate.convertAndSend("seckillExchange", "secondkill.order", JSON.toJSONString(order));
    }

}
