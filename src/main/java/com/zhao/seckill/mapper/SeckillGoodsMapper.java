package com.zhao.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhao.seckill.domain.pojo.SeckillGoods;
import com.zhao.seckill.vo.SeckillGoodsDetailVo;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */

@Repository
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {

    IPage<SeckillGoodsDetailVo> list(Page<SeckillGoodsDetailVo> page);

    SeckillGoodsDetailVo selectById(Long goodId);
}
