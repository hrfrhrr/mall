package com.baizhan.mall.user.controller;

import com.baizhan.mall.common.core.result.Result;
import com.baizhan.mall.common.security.UserContext;
import com.baizhan.mall.user.entity.Address;
import com.baizhan.mall.user.entity.ShoppingUser;
import com.baizhan.mall.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户中心")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "当前登录用户信息（服务级 JWT 鉴权注入）")
    @GetMapping("/me")
    public Result<ShoppingUser> me() {
        return Result.success(userService.getById(UserContext.getUserId()));
    }

    @Operation(summary = "收货地址列表")
    @GetMapping("/addresses")
    public Result<List<Address>> addresses() {
        return Result.success(userService.listAddresses(UserContext.getUserId()));
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/addresses")
    public Result<Long> createAddress(@Valid @RequestBody Address address) {
        return Result.success(userService.createAddress(UserContext.getUserId(), address));
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/addresses/{addressId}")
    public Result<Void> removeAddress(@PathVariable Long addressId) {
        userService.removeAddress(UserContext.getUserId(), addressId);
        return Result.success();
    }
}
