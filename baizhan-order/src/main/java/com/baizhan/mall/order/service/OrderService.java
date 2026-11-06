package com.baizhan.mall.order.service;

import com.baizhan.mall.api.goods.GoodsApi;
import com.baizhan.mall.api.goods.GoodsDTO;
import com.baizhan.mall.common.core.exception.BizException;
import com.baizhan.mall.common.core.util.IdUtils;
import com.baizhan.mall.order.entity.Orders;
import com.baizhan.mall.order.mapper.OrdersMapper;
import com.baizhan.mall.order.mq.OrderTimeoutProducer;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务。
 * 下单链路（演示版，逐步向高并发演进）：
 *   1. Dubbo 调商品服务取价（生产加 Sentinel 熔断 + 本地缓存兜底）
 *   2. 生成订单号（雪花算法）落库，状态「未付款」
 *   3. 发 RocketMQ 延迟消息，30 分钟未支付自动关单
 */
@Slf4j
@Service
public class OrderService {

    private final OrdersMapper ordersMapper;
    private final ObjectProvider<OrderTimeoutProducer> timeoutProducer;

    /** 商品服务 Dubbo 引用（Nacos 服务发现） */
    @DubboReference(version = "1.0.0")
    private GoodsApi goodsApi;

    @Value("${baizhan.order.expire-minutes:30}")
    private int expireMinutes;

    public OrderService(OrdersMapper ordersMapper,
                        ObjectProvider<OrderTimeoutProducer> timeoutProducer) {
        this.ordersMapper = ordersMapper;
        this.timeoutProducer = timeoutProducer;
    }

    /** 查询商品服务（Dubbo RPC） */
    private GoodsDTO getGoods(Long goodsId) {
        try {
            GoodsDTO goods = goodsApi.getGoods(goodsId);
            if (goods == null || !Integer.valueOf(1).equals(goods.getIsMarketable())) {
                throw new BizException("商品不存在或已下架");
            }
            return goods;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[订单] 调用商品服务失败 goodsId={}", goodsId, e);
            throw new BizException("商品服务暂时不可用，请稍后重试");
        }
    }

    /** 创建订单（简化：单商品单件，金额以服务端价格为准，严禁信任前端传价） */
    public String create(Long userId, String username, Long goodsId, Long addressId) {
        GoodsDTO goods = getGoods(goodsId);
        BigDecimal price = goods.getPrice();

        Orders order = new Orders();
        order.setId(IdUtils.nextIdStr());
        order.setPayment(price);
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setExpire(LocalDateTime.now().plusMinutes(expireMinutes));
        order.setUserId(userId);
        order.setBuyerNick(username);
        // 地址快照由 baizhan-user 服务获取（演示省略，直接留空）
        ordersMapper.insert(order);

        timeoutProducer.ifAvailable(producer -> producer.sendTimeoutCheck(order.getId()));
        log.info("[下单成功] orderId={}, userId={}, payment={}", order.getId(), userId, price);
        return order.getId();
    }

    public Orders detail(String orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    public IPage<Orders> page(Long userId, long current, long size) {
        return ordersMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .orderByDesc(Orders::getCreateTime));
    }

    public List<Orders> timeoutUnpaidOrders() {
        return ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, 1)
                .lt(Orders::getExpire, LocalDateTime.now()));
    }

    public void closeOrder(String orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order != null && Integer.valueOf(1).equals(order.getStatus())) {
            order.setStatus(6);
            ordersMapper.updateById(order);
            log.info("[关单成功] orderId={}", orderId);
        }
    }
}
