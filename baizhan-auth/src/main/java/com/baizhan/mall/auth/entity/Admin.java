package com.baizhan.mall.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bz_admin")
public class Admin {

    @TableId(type = IdType.AUTO)
    private Long aid;

    private String username;

    /** BCrypt 密文 */
    private String password;
}
