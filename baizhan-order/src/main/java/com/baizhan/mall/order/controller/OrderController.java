package com.baizhan.mall.order.controller;

import com.baizhan.mall.common.core.result.PageResult;
import com.baizhan.mall.common.core.result.Result;
import com.baizhan.mall.common.security.UserContext;
import com.baizhan.mall.order.entity.Orders;
import com.baizhan.mall.order.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单中心")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<String> create(@RequestParam Long goodsId,
                                 @RequestParam(required = false) Long addressId) {
        return Result.success(orderService.create(UserContext.getUserId(),
                UserContext.getUsername(), goodsId, addressId));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<Orders> detail(@PathVariable String orderId) {
        return Result.success(orderService.detail(orderId, UserContext.getUserId()));
    }

    @Operation(summary = "我的订单分页")
    @GetMapping("/page")
    public Result<PageResult<Orders>> page(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size) {
        IPage<Orders> page = orderService.page(UserContext.getUserId(), current, size);
        return Result.success(PageResult.of(page));
    }
}
