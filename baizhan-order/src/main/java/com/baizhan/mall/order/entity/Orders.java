package com.baizhan.mall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bz_orders")
public class Orders {

    @TableId
    private String id;

    /** 实付金额 */
    private BigDecimal payment;

    /** 1 微信 2 支付宝 */
    private Integer paymentType;

    /** 1 未付款 2 已付款 3 未发货 4 已发货 5 交易成功 6 交易关闭 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime paymentTime;

    private LocalDateTime expire;

    private Long userId;

    private String buyerMessage;

    private String buyerNick;

    private String receiverAreaName;

    private String receiverMobile;

    private String receiver;

    private String receiverZipCode;
}
