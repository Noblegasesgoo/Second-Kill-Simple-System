package com.zhao.seckill.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.seckill.domain.pojo.Goods;
import com.zhao.seckill.vo.GoodsVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author noblegasesgoo
 * @since 2022-02-14
 */
public interface IGoodsService extends IService<Goods> {

    IPage<GoodsVo> list(Page<GoodsVo> page);
}
