package com.baizhan.mall.order.mq;

import com.baizhan.mall.order.config.RocketMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;

/**
 * 订单超时关单消息生产者。
 * 发送延迟消息（delayLevel=18 约 30 分钟），由消费者监听后关闭超时未支付订单。
 * 未配置 RocketMQ 地址时该 Bean 不创建，系统降级为同步下单。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "baizhan.mq", name = "enabled", havingValue = "true")
public class OrderTimeoutProducer {

    /** 30 分钟（delayLevel 18 ≈ 30min） */
    private static final int DELAY_LEVEL_30M = 18;

    private final RocketMqConfig config;
    private DefaultMQProducer producer;

    public OrderTimeoutProducer(RocketMqConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void start() throws MQClientException {
        producer = new DefaultMQProducer(config.getProducerGroup());
        producer.setNamesrvAddr(config.getNameAddr());
        producer.setSendMsgTimeout(5000);
        producer.setRetryTimesWhenSendFailed(2);
        producer.start();
        log.info("[RocketMQ] 生产者已启动 namesrv={}, topic={}",
                config.getNameAddr(), config.getOrderTimeoutTopic());
    }

    /** 发送订单超时检查消息 */
    public void sendTimeoutCheck(String orderId) {
        try {
            Message message = new Message(
                    config.getOrderTimeoutTopic(),
                    "orderTimeout",
                    orderId.getBytes(StandardCharsets.UTF_8));
            message.setDelayTimeLevel(DELAY_LEVEL_30M);
            producer.send(message);
            log.info("[RocketMQ] 超时关单消息已发送 orderId={}", orderId);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            // 消息发送失败不阻断主流程，由对账任务兜底
            log.warn("[RocketMQ] 消息发送失败 orderId={}", orderId, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
