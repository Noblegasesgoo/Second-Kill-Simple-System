package com.zhao.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.seckill.domain.pojo.SeckillGoods;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
public interface ISeckillGoodsService extends IService<SeckillGoods> {

    /**
     * 查询秒杀商品列表
     * @param page
     * @return 秒杀商品列表
     */
    IPage<SeckillGoodsDetailVo> list(Page<SeckillGoodsDetailVo> page);

    /**
     * 通过id查询秒杀商品详情
     * @param goodsId
     * @return 秒杀商品详情
     */
    SeckillGoodsDetailVo ListById(Long goodsId);

    /**
     * 检查库存以及预减库存
     * @param goodsId
     * @return 是否还有库存
     */
    Boolean checkStockAndDecrement(Long goodsId);
}
