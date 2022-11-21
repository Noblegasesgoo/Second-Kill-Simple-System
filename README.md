**windows本机硬件配置：**

- 8核32G

![在这里插入图片描述](https://img-blog.csdnimg.cn/c7712fd2eb754488b791336e7f922e72.png)


**linux服务器硬件配置：**

- 2核4G



前言，此次优化的简易秒杀系统没有涉及到分布式，自然也就没有主从复制之类的技术点，到这里我们应该都拥有了自己的一个秒杀接口demo，我们围绕着这个demo开始来进行力所能及的优化，样本都是10件库存。

- **QPS（Query Per Second）**：每秒查询率，一台服务器，每秒能够查询的次数，是特定的查询服务器在规定时间内所处理流量的能力的衡量标准，通俗来说**就是服务器在一秒的时间内处理了多少个请求**。
- **TPS（Transactions Per Second）**：每秒处理的事务数目。**这里的一个事务**是指一个客户机向服务器发送请求然后服务器做出反应的过程**(完整处理，即客户端发起请求到得到响应)**。客户机在发送请求时开始计时，收到服务器响应后结束计时，以此来计算使用的时间和完成的事务个数，最终利用这些信息作出的评估分。一个事务可能对应多个请求，可以参考下数据库的事务操作。



**秒杀接口第一次压测：**

```shell
/usr/local/jmeter/apache-jmeter-5.3/bin/jmeter -n -t /usr/local/jmeter/jmx/first-kill-test.jmx -l result.jtl

./jmeter.sh -n -t first-kill-test.jmx -l result.jtl
```

条件：1000个线程10次循环，共计10000次访问。

- Windows：

  - QPS：984.5|4384
  - 数据库错误，出现超卖问题。

- Linux：

  - QPS：526.4
  - 数据库错误，出现超卖问题。
![在这里插入图片描述](https://img-blog.csdnimg.cn/75a7043868c64bc19c376273dada028e.png)

## 压测秒杀接口出现的问题

- 数据库出现==**超卖问题**==。
- 订单混乱。
- QPS不高，不能承受更大的并发。



## 分析原因

- 两个系统的差别，windows 一般来说会慢一点，但是我的电脑配置比较高，所以会导致出现比 linux 快的情况。
- 有对数据的操作，并且是直接访问数据库操作，未使用缓存。
- 没有锁，导致数据混乱。



## 优化完成之后的请求代码

以下代码块，是我们优化过后的秒杀请求代码块，接下来会逐一讲解每一个优化步骤。

```java
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
```



## 优化一：页面资源优化方法

由于现在页面渲染，都是得向数据库请求建立连接，效率肯定没有内存来的快，而且目前的瓶颈就是数据库的性能瓶颈，我们可以先把一些不怎么变动的数据，或者不敏感的数据，或者对数据一致性要求没那么高的数据，按照所需粒度划分，将其存入缓存，之后页面请求就先访问缓存，速度会快很多，怎么做？那么就是简单的 redis 应用，选取所需的 Redis 数据类型将其放入 redis 中即可，这里就不细说了。



## 优化二：解决超卖问题（重点）

在代码中，我们可以很清楚的找到关于超卖问题的核心代码块：

```java
/** 判断是否重复购买 **/
Boolean aBoolean = seckillOrderService.checkCurrentUserOrder(userId);
if (aBoolean) {
    return Response.setResponse(StatusCode.REPEAT_ORDER);
}

/** 从秒杀商品表中查询出当前正在被秒杀的对应商品，然后执行减库存操作 **/
LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(SeckillGoods::getGoodsId, goods.getId());

SeckillGoods currentSeckillGoods = seckillGoodsService.getOne(wrapper);
currentSeckillGoods.setStockCount(currentSeckillGoods.getStockCount() - 1);
seckillGoodsService.updateById(currentSeckillGoods);
```

关于库存的变化，就是根据以上俩不同方法中的代码块来就行的，那么，我们学过关系型数据库中的MySQL数据库的一些基础知识了对吧，我们可以先考虑数据库层面的解决。

### 思考过程

可以不可以用原子类？我个人认为是可以用的，但是涉及到**cas**，所以如果并发情况下并行的情况多，那么同时**cas**操作自旋的情况就会变多，**cpu**占用率会很高，这对一些硬件不太好的机器不太友好，所以我就先想到了数据库层面去解决。

在之前的学习中写过一个注册功能，那个注册功能可以通过邮箱，手机号，用户名三种不同的数据类型作为账户传入，但是这仨东西在当今社会基本上都是每个人对应不同的，也就是说是一对一的关系，自然也就不能有重复的，但是如果我在代码逻辑中处理的话，就会涉及到多次查询数据库，无意之间增加了数据库的压力，但是我当时又没有更好的办法，比较才学疏浅，于是我想到了唯一索引，这个东西不允许字段内容重复，我就尝试去对这几个字段建立了唯一索引，然后我再次测试，发现如果已经存在的数据，在唯一索引的约束下，是插入不进去的，类似于一个乐观锁，我认为我可以插入成功。

所以，到这，我们可以尝试对订单中的**商品id**以及对应的**用户id**进行唯一索引的建立，果不其然，测试之后，是可以成功控制库存的，但是，这还是直接对数据库进行操作，难免会承受不了大量的访问，属于是高开低走了，而且这里订单与库存数不等问题没有解决，这种方法还是会出现重复修改的情况。

而我们在学习过 **redis** 基础之后，可以了解到 **redis** 的 **`incr`** 是原子性自增的，我们或许可以将秒杀商品的库存弄到 **redis** 中，我们要考虑如果用户秒杀一次以后，如果缓存失效后再抢一次就重复了的情况，所以**默认秒杀时间一定要小于缓存存活时间**。

有了**默认秒杀时间一定要小于缓存存活时间**这个条件，我们就可以开始操作了，首先，想到了 **`incr`**，那么**对应的就是想到直接在 redis 中进行库存的增减操作**，但是又引出了以下两个问题：

- ==我们怎么让秒杀商品的库存提前进入 **redis** ？==
- ==缓存和数据库的数据最终一致性怎么保证？==
- ==如何保证订单不重复？并且不超卖呢？==
- ==如何能够减少缓存的访问次数呢？==

#### 问题一解决

我们一一来慢慢解决，首先设计一个第一个问题的解决方法：

1. 我首先想到的是在项目启动前直接初始化 redis，但是得不更新缓存数据，用定时任务挺好办的，但是一般都涉及多线程，这样的话，本来我们的服务器的硬件不够，还使用多线程，或者长期维护一个线程池，开销也是挺大的，所以这个办法我个人暂且否定了。
2. 那么我想到的第二个方法，就是简单粗暴，写一个接口，那个接口直接更新缓存，但是我们也得定期同步，还是得维护一个线程池。

但是转念一想，我们现在就是盯着优化，至于这种上面这种情况还是，不去深究，我们就采用一个接口直接更新缓存吧，之后再去思考延迟双删的问题：

- **解决库存进入 Redis 的代码块：**

```java
@ApiOperation(value = "进行秒杀商品的库存缓存内更新请求" )
@PostMapping("/public/update/cache")
public Response updateCache() {

    IPage<SeckillGoodsDetailVo> list = seckillGoodsService.list(new Page<>(0, -1));
    List<SeckillGoodsDetailVo> records = list.getRecords();
    records.stream().forEach((record) -> {
        /** 设置秒杀商品缓存，一般情况下，缓存存在时间要大于秒杀总时间 **/
        redisTemplate.opsForValue().set( "SECONDKILL::PRODUCT:" + record.getId() + "-STOCK::COUNT:", record.getStockCount(), 1, TimeUnit.DAYS);
    });

    return Response.success().message("秒杀商品库存缓存更新成功");
}
```



#### 问题二解决

缓存和数据库的数据最终一致性如何解决？

- 首先 SpringBoot 项目它的内置 tomcat  能够容纳线程的数量不算多，在高并发环境下，可能就会用完，那么此时之后的所有请求都得等待前面的请求完成之后再处理，而前面的请求可能涉及数据库操作，那么 mysql 数据库本来并发量就不高，所以高并发环境下很可能会出现阻塞的情况。
- 而为了更快的响应请求，我们采取异步请求的方式，保证数据最终一致性即可，这样可以大幅减少我们数据库的压力。

而这个异步请求的方式，我们可以通过消息队列去解决，这里我们可以通过几张图来快速理解为什么需要异步请求，为什么异步请求能够给高并发系统带来更多的请求承载量：
![在这里插入图片描述](https://img-blog.csdnimg.cn/4a96f73f4f11473b92235794f161e506.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBATm9ibGVnYXNlc2dvbw==,size_20,color_FFFFFF,t_70,g_se,x_16)

![在这里插入图片描述](https://img-blog.csdnimg.cn/1da59886dcb54ba08b068c13b67a3dec.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBATm9ibGVnYXNlc2dvbw==,size_20,color_FFFFFF,t_70,g_se,x_16)


到了使用多线程异步，我们得考虑自身电脑硬件问题，以及耦合度的问题，此时我们就引出了消息中间件：
![在这里插入图片描述](https://img-blog.csdnimg.cn/6971965d96f64445ad6da3428aeb3489.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBATm9ibGVnYXNlc2dvbw==,size_20,color_FFFFFF,t_70,g_se,x_16)
所以问题二的解决方法，我们当前可以简易的去使用消息队列来完成。



#### 问题三解决

通过上面那个问题的代码块我们解决了**秒杀商品的库存缓存内更新的问题**，下一步我们要解决保证订单不重复，不超卖问题：

我是这样想的，通过上一个问题的解决方法，给我们缓存中带来了秒杀商品的库存缓存数据，我们可以利用 **`redis 的 incr 操作`**来原子更新库存问题，这样就不会涉及线程不安全，而且我们强制库存只能先从缓存扣除，然后异步同步到数据库。

我们首先根据库存在缓存中的 key 进行查询当前最新库存，得到当前最新库存之后，进行合法性判断，合法之后，进行 incr 的原子自增来完成最后的减少库存操作，最后，我们使用消息队列去异步同步数据到数据库。

- **解决不超卖问题的代码块：**

```java
@Override
public Boolean checkStockAndDecrement(Long goodsId) {

   String key = "SECONDKILL::PRODUCT:" + goodsId + "-STOCK::COUNT";
        Integer stock = (Integer) redisTemplate.opsForValue().get(key);

        if (null == stock) {
            /** 秒杀商品或已下架 **/
            return false;
        }

        if (stock <= 0) {
            /** 没有库存了 **/
            return false;
        }

        /** 这里 decrement 的话，会将库存减到负一而并非我们需要的零，所以使用 increment 反向自增 **/
        Long increment = redisTemplate.opsForValue().increment(key, -1);
        if (increment >= 0) {

            /** mq异步保证数据库和缓存库存数量的最终一致 **/
            secondKillSender.toDoUpdateStock(goodsId);
            return true;
        }else {

            /** 到这也就是秒杀失败了 **/
            /** 为什么失败呢？因为在此之前第一次查看库存与减库存之间有线程抢先修改库存导致库存不足，我们这里要二次判断 **/
            /** 为了保证数据的线程安全，我们要回退数据 **/
            redisTemplate.opsForValue().increment(key,1);
            return false;
        }
}
```

#### 问题四解决

访问缓存也是需要开销的，那么我们怎么去减少缓存的访问次数呢？为了减少缓存的访问次数，我们可以设置一个标记，标记缓存中的关键数据：

```java
private volatile Map<String, Boolean> stockLocalOverMap = new HashMap<>();
```

为什么要加上 **volatile** 关键字呢？我们都知道 **volatile** 关键字它保证可见性和有序性，但是不保证原子性，但是它无法保证多线程的执行有序性。任何被 **volatile** 修饰的变量，都不拷贝副本到工作内存，任何修改都及时写在主存，所以我们假设多线程情景下，这个内存标记在内存中一旦被改变，接下来的线程就可见，而且也减少了别的线程拷贝，修改，写回主存的时间。

```java
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
```

```java
/** 检查库存以及预减库存 **/
Map<String, Boolean> stockLocalOverMap = seckillGoodsService.getStockLocalOverMap();
if (!stockLocalOverMap.get(goods.getId().toString())) {
    return Response.error().message("秒杀结束！");
}

Boolean hasStock =  seckillGoodsService.checkStockAndDecrement(goods.getId());
if (!hasStock) {
    return Response.error().message("秒杀结束！");
}
```



## 第一次优化结果
我们查看 5000 个线程循环 10 次的结果：
![在这里插入图片描述](https://img-blog.csdnimg.cn/7fcdf36094414542929e086aac5cce8f.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/5fa6b6964fce4e37b1199c80ee5e85b6.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBATm9ibGVnYXNlc2dvbw==,size_19,color_FFFFFF,t_70,g_se,x_16)
![在这里插入图片描述](https://img-blog.csdnimg.cn/93f683b72a794812a60f5802d1471a00.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBATm9ibGVnYXNlc2dvbw==,size_20,color_FFFFFF,t_70,g_se,x_16)
吞吐量高达惊人的 `9129.1`，这个根据每个人的电脑硬件不同，测得的结果都不同，其次就是最关键的数据库以及 redis 中的数据是否一致，以及有没有出现超卖问题，很显然没有出现，说明我们解决了当前的这个超卖问题的同时也提升了并发量，但是**这个吞吐量也是根据秒杀商品的库存数量来定的，我只用了单数据库，如果秒杀商品库存超过2倍单个数据库最大连接数，它的吞吐量就会下降一半作用，所以秒杀的时候适当选取样本数量也是很重要的事情**。


我将这次优化的所有内容都展示出来，层级从上到下，从内到外，注释清晰

### 秒杀主入口

- 进行秒杀请求

```java
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
```

### 判断是否重复购买的接口

- 查询当前用户是否重复下单

```java
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
```

### 检查库存以及预减库存接口

- 检查库存以及预减库存

```java
/**
* 检查库存以及预减库存
* @param goodsId
* @return 是否还有库存
*/
@Override
public Boolean checkStockAndDecrement(Long goodsId) {

    String key = "SECONDKILL::PRODUCT:" + goodsId + "-STOCK::COUNT";
    Integer stock = (Integer) redisTemplate.opsForValue().get(key);

    if (null == stock) {
        /** 秒杀商品或已下架 **/
        return false;
    }

    if (stock <= 0) {
        /** 没有库存了 **/
        /** 对应内存标记设为 false **/
        this.stockLocalOverMap.put(goodsId.toString(), false);
        return false;
    }

    /** 这里 decrement 的话，会将库存减到负一而并非我们需要的零，所以使用 increment 反向自增 **/
    Long increment = redisTemplate.opsForValue().increment(key, -1);
    if (increment >= 0) {

        /** mq异步保证数据库和缓存库存数量的最终一致 **/
        secondKillSender.toDoUpdateStock(goodsId);
        return true;
    } else {

        /** 到这也就是秒杀失败了 **/
        /** 为什么失败呢？因为在此之前第一次查看库存与减库存之间有线程抢先修改库存导致库存不足，我们这里要二次判断 **/
        /** 为了保证数据的线程安全，我们要回退数据 **/
        /** 有点像DCL **/
        redisTemplate.opsForValue().increment(key,1);
        return false;
    }
}
```

### 秒杀成功的下单接口

- 秒杀订单接口

```java
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
```

- 其中的订单预入方法

```java
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
```

### mq异步配置

- 配置类

```java
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
```

- mq的生产者类

```java
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
        rabbitTemplate.convertAndSend("secondkillExchange", "secondkill.stock", goodsId.toString());
    }

    /**
     * 异步更新订单消息（数据库）
     * @param order
     */
    public void toDoUpdateOrder(Order order) {
        rabbitTemplate.convertAndSend("secondkillExchange", "secondkill.order", JSON.toJSONString(order));
    }
}
```

- mq的消费者类

```java
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
```
