package com.baizhan.mall.auth.entity;

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

    /** BCrypt 密文 */
    private String password;

    private String phone;

    private String nickName;

    private String name;

    /** Y 正常 / N 非正常 */
    private String status;

    private String headPic;

    private String sex;
}
