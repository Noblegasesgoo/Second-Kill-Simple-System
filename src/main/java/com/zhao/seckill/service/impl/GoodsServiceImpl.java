package com.zhao.seckill.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.seckill.domain.pojo.Goods;
import com.zhao.seckill.mapper.GoodsMapper;
import com.zhao.seckill.service.IGoodsService;
import com.zhao.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public IPage<GoodsVo> list(Page<GoodsVo> page) {

        return goodsMapper.selectPage(page);
    }
}
