package com.baizhan.mall.api.goods;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品跨服务传输对象（Dubbo RPC 需序列化）。
 */
@Data
public class GoodsDTO implements Serializable {

    private Long id;

    private String goodsName;

    private String caption;

    private BigDecimal price;

    private Long brandId;

    private Long productType1Id;

    private Long productType2Id;

    private Long productType3Id;

    private String headerPic;

    /** 是否上架 0否 1是 */
    private Integer isMarketable;
}
