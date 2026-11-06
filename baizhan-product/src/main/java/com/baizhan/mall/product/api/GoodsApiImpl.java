package com.baizhan.mall.product.api;

import com.baizhan.mall.api.goods.GoodsApi;
import com.baizhan.mall.api.goods.GoodsDTO;
import com.baizhan.mall.product.entity.Goods;
import com.baizhan.mall.product.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;

/**
 * 商品 Dubbo 服务实现（provider）。
 */
@Slf4j
@DubboService(version = "1.0.0")
public class GoodsApiImpl implements GoodsApi {

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public GoodsDTO getGoods(Long goodsId) {
        if (goodsId == null) {
            return null;
        }
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return null;
        }
        GoodsDTO dto = new GoodsDTO();
        BeanUtils.copyProperties(goods, dto);
        return dto;
    }
}
