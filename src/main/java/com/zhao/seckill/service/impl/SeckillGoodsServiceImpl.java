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
            return false;
        }

        /** 这里 decrement 的话，会将库存减到负一而并非我们需要的零，所以使用 increment 反向自增 **/
        redisTemplate.opsForValue().increment(key, -1);

        /** mq异步保证数据库和缓存库存数量的最终一致 **/
        secondKillSender.toDoUpdateStock(goodsId);

        return true;
    }
}
