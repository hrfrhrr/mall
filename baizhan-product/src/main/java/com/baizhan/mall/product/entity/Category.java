package com.baizhan.mall.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 首页广告位（bz_category） */
@Data
@TableName("bz_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String url;

    private String pic;

    /** 0 未启用 1 启用 */
    private Integer status;
}
