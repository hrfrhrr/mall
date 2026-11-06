package com.baizhan.mall.api.goods;

/**
 * 商品 Dubbo 服务接口。
 * 由商品服务（provider）实现、订单服务（consumer）引用，
 * 替代原先的 RestTemplate HTTP 调用。
 */
public interface GoodsApi {

    /** 按ID查询商品，不存在返回 null */
    GoodsDTO getGoods(Long goodsId);
}
