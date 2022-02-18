package com.zhao.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/17 20:51
 * @description RabbitMQ配置类
 */

@Configuration
public class RabbitMQConfig {

    private static final String ORDER_QUEUE = "orderQueue";
    private static final String GOODS_QUEUE = "goodsQueue";

    private static final String SECONDKILL_EXCHANGE = "secondkillExchange";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Queue goodsQueue() {
        return new Queue(GOODS_QUEUE, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(SECONDKILL_EXCHANGE);
    }

    @Bean
    public Binding orderQueueBind() {
        return BindingBuilder.bind(orderQueue()).to(topicExchange()).with("secondkill.order");
    }

    @Bean
    public Binding goodsQueueBind() {
        return BindingBuilder.bind(goodsQueue()).to(topicExchange()).with("secondkill.stock");
    }
}
