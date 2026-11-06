package com.baizhan.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("bz_goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String goodsName;

    private String caption;

    private BigDecimal price;

    private Long brandId;

    private Long productType1Id;

    private Long productType2Id;

    private Long productType3Id;

    private String headerPic;

    private String introduction;

    /** 是否上架：0 否 1 是 */
    private Integer isMarketable;
}
