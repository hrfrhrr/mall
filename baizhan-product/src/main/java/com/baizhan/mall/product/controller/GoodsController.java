package com.baizhan.mall.product.controller;

import com.baizhan.mall.common.core.result.PageResult;
import com.baizhan.mall.common.core.result.Result;
import com.baizhan.mall.product.entity.Category;
import com.baizhan.mall.product.entity.Goods;
import com.baizhan.mall.product.service.GoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品中心")
@RestController
@RequestMapping("/api/product")
public class GoodsController {

    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @Operation(summary = "商品分页")
    @GetMapping("/goods/page")
    public Result<PageResult<Goods>> page(@RequestParam(defaultValue = "1") long current,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String goodsName) {
        IPage<Goods> page = goodsService.page(current, size, goodsName);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "商品详情（带 Redis 缓存）")
    @GetMapping("/goods/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        return Result.success(goodsService.detail(id));
    }

    @Operation(summary = "更新商品（演示：删缓存）")
    @PutMapping("/goods")
    public Result<Void> update(@RequestBody Goods goods) {
        goodsService.update(goods);
        return Result.success();
    }

    @Operation(summary = "首页广告位")
    @GetMapping("/ads")
    public Result<List<Category>> ads() {
        return Result.success(goodsService.ads());
    }
}
