package com.baizhan.mall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "baizhan.mq")
public class RocketMqConfig {

    /** 是否启用 MQ（rocketmq.name-addr 为空时自动关闭） */
    private boolean enabled = false;

    /** NameServer 地址 */
    private String nameAddr = "";

    /** 生产者组 */
    private String producerGroup = "baizhan-order-group";

    /** 订单超时关单 Topic */
    private String orderTimeoutTopic = "baizhan-order-timeout";
}
