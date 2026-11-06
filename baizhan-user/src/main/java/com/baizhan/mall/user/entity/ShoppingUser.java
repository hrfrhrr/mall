package com.baizhan.mall.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bz_shopping_user")
public class ShoppingUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String phone;

    private String nickName;

    private String name;

    private String status;

    private String headPic;

    private String sex;
}
