package com.baizhan.mall.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bz_address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String provinceName;

    private String cityName;

    private String areaName;

    private String mobile;

    private String address;

    private String contact;

    private String zipCode;
}
