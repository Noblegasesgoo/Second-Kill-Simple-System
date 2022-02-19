package com.zhao.seckill.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.seckill.domain.pojo.SeckillGoods;
import com.zhao.seckill.mapper.SeckillGoodsMapper;
import com.zhao.seckill.service.ISeckillGoodsService;
import com.zhao.seckill.service.mq.SecondKillSender;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements ISeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecondKillSender secondKillSender;

    /** 内存标记 **/
    /** 为什么要加上 volatile 关键字呢？我们都知道 volatile 关键字它保证可见性和有序性，但是不保证原子性 **/
    /** 但是它无法保证多线程的执行有序性。任何被volatile修饰的变量，都不拷贝副本到工作内存，任何修改都及时写在主存 **/
    /** 所以我们假设多线程情景下，这个内存标记在内存中一旦被改变，接下来的线程就可见，而且也减少了别的线程拷贝，修改，写回主存的时间 **/
    private volatile Map<String, Boolean> stockLocalOverMap = new HashMap<>();

    /**
     * 获得秒杀商品库存的内存标记
     * @return 秒杀商品库存的内存标记
     */
    public Map<String, Boolean> getStockLocalOverMap() {
        return this.stockLocalOverMap;
    }

    /**
     * 查询秒杀商品列表
     * @param page
     * @return 秒杀商品列表
     */
    @Override
    public IPage<SeckillGoodsDetailVo> list(Page<SeckillGoodsDetailVo> page) {

        IPage<SeckillGoodsDetailVo> list = seckillGoodsMapper.list(page);
        return list;
    }

    /**
     * 通过id查询秒杀商品详情
     * @param goodsId
     * @return 秒杀商品详情
     */
    @Override
    public SeckillGoodsDetailVo ListById(Long goodsId) {

        SeckillGoodsDetailVo seckillGoods = seckillGoodsMapper.selectById(goodsId);
        return seckillGoods;
    }

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
        }else {

            /** 到这也就是秒杀失败了 **/
            /** 为什么失败呢？因为在此之前第一次查看库存与减库存之间有线程抢先修改库存导致库存不足，我们这里要二次判断 **/
            /** 为了保证数据的线程安全，我们要回退数据 **/
            /** 有点像DCL **/
            redisTemplate.opsForValue().increment(key,1);
            return false;
        }
    }
}
