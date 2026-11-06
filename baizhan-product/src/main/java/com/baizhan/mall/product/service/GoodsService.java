package com.baizhan.mall.product.service;

import com.baizhan.mall.common.core.exception.BizException;
import com.baizhan.mall.product.entity.Category;
import com.baizhan.mall.product.entity.Goods;
import com.baizhan.mall.product.mapper.CategoryMapper;
import com.baizhan.mall.product.mapper.GoodsMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务。演示「缓存旁路」策略：
 * 读 -> 查 Redis -> 未命中查 DB 并回填（TTL 加随机值防雪崩）；
 * 写 -> 更新 DB -> 删除缓存。
 */
@Slf4j
@Service
public class GoodsService {

    private static final String GOODS_CACHE_KEY = "baizhan:goods:";
    private static final String ADS_CACHE_KEY = "baizhan:ads";

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public GoodsService(GoodsMapper goodsMapper, CategoryMapper categoryMapper,
                        RedisTemplate<String, Object> redisTemplate) {
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
    }

    public IPage<Goods> page(long current, long size, String goodsName) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(goodsName != null && !goodsName.isBlank(), Goods::getGoodsName, goodsName)
                .eq(Goods::getIsMarketable, 1)
                .orderByDesc(Goods::getId);
        return goodsMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @SuppressWarnings("unchecked")
    public Goods detail(Long id) {
        String key = GOODS_CACHE_KEY + id;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (Goods) cached;
        }
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        long ttl = 30 + ThreadLocalRandom.current().nextInt(10); // 30~40 分钟随机 TTL
        redisTemplate.opsForValue().set(key, goods, ttl, TimeUnit.MINUTES);
        return goods;
    }

    public void update(Goods goods) {
        goodsMapper.updateById(goods);
        redisTemplate.delete(GOODS_CACHE_KEY + goods.getId());
    }

    @SuppressWarnings("unchecked")
    public List<Category> ads() {
        Object cached = redisTemplate.opsForValue().get(ADS_CACHE_KEY);
        if (cached != null) {
            return (List<Category>) cached;
        }
        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getId));
        redisTemplate.opsForValue().set(ADS_CACHE_KEY, list, 10, TimeUnit.MINUTES);
        return list;
    }
}
