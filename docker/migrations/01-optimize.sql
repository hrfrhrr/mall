-- ============================================================
-- 百战电商 · 数据库优化迁移（在 baizhanshopping.sql 之后执行）
-- 内容：1.脏数据清理  2.字段类型规范  3.高频查询索引补齐  4.utf8mb4 统一
-- 说明：一次性迁移脚本，请勿重复执行；适用于 MySQL 8.0
-- ============================================================
USE baizhanshopping;

-- ---------- 1. 脏数据清理（为唯一约束铺路） ----------
-- 1.1 bz_admin 重复用户名：保留 aid 最小的一条（原数据中 'sxt' 重复）
DELETE t1 FROM bz_admin t1
  JOIN (SELECT username, MIN(aid) AS keep_id FROM bz_admin
        GROUP BY username HAVING COUNT(*) > 1) d
  ON t1.username = d.username AND t1.aid <> d.keep_id;

-- 1.2 bz_goods_specification_option 存在重复规格项且无主键：重建表 + 复合主键
RENAME TABLE bz_goods_specification_option TO bz_gso_backup;
CREATE TABLE bz_goods_specification_option (
  `gid` bigint(20) NOT NULL COMMENT '商品id',
  `optionId` bigint(20) NOT NULL COMMENT '规格项id',
  PRIMARY KEY (`gid`, `optionId`) USING BTREE,
  INDEX `idx_option_id`(`optionId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '商品-规格项关联表';
INSERT IGNORE INTO bz_goods_specification_option (`gid`, `optionId`)
  SELECT `gid`, `optionId` FROM bz_gso_backup;
DROP TABLE bz_gso_backup;

-- ---------- 2. 唯一约束 ----------
-- 管理员登录按用户名查询，必须唯一（原表无唯一约束）
ALTER TABLE bz_admin ADD UNIQUE INDEX `uk_username`(`username`) USING BTREE;

-- ---------- 3. 字段类型规范 ----------
-- 3.1 订单：枚举字段由 varchar(1) 改 tinyint，邮费由 varchar 改 decimal
ALTER TABLE bz_orders
  MODIFY `paymentType` tinyint(4) NULL DEFAULT NULL COMMENT '支付方式 1、微信支付 2、支付宝支付',
  MODIFY `status` tinyint(4) NULL DEFAULT NULL COMMENT '状态：1未付款 2已付款 3未发货 4已发货 5交易成功 6交易关闭 7待评价',
  MODIFY `postFee` decimal(10, 2) NULL DEFAULT NULL COMMENT '邮费，单位元';

-- 3.2 秒杀表加乐观锁版本号（配合 UPDATE ... WHERE stockCount>0 AND version=? 防超卖）
ALTER TABLE bz_seckill_goods
  ADD COLUMN `version` int(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER `stockCount`;

-- 3.3 购物车补 userId（原表只能通过 orderId 反查，未下单的购物车无法按用户查）
ALTER TABLE bz_cart_goods
  ADD COLUMN `userId` bigint(20) NULL DEFAULT NULL COMMENT '用户id' AFTER `num`;

-- ---------- 4. 高频查询索引补齐 ----------
-- 4.1 商品列表：WHERE isMarketable=1 ORDER BY id DESC（消除 filesort）
--     及品牌/一级类目筛选
ALTER TABLE bz_goods
  ADD INDEX `idx_marketable_id`(`isMarketable`, `id`) USING BTREE,
  ADD INDEX `idx_brand_id`(`brandId`) USING BTREE,
  ADD INDEX `idx_product_type1_id`(`productType1Id`) USING BTREE;

-- 4.2 我的订单：WHERE userId=? ORDER BY createTime DESC
--     超时关单任务：WHERE status=1 AND expire<NOW()
ALTER TABLE bz_orders
  ADD INDEX `idx_user_create`(`userId`, `createTime`) USING BTREE,
  ADD INDEX `idx_status_expire`(`status`, `expire`) USING BTREE;
-- 原 status 单列索引已被 (status, expire) 复合索引完全覆盖，删除以减少写入开销
ALTER TABLE bz_orders DROP INDEX `status`;

-- 4.3 商品详情图、收货地址列表、购物车按订单/用户查
ALTER TABLE bz_goods_image ADD INDEX `idx_goods_id`(`goodsId`) USING BTREE;
ALTER TABLE bz_address ADD INDEX `idx_user_id`(`userId`) USING BTREE;
ALTER TABLE bz_payment ADD INDEX `idx_order_id`(`orderId`) USING BTREE;
ALTER TABLE bz_cart_goods
  ADD INDEX `idx_order_id`(`orderId`) USING BTREE,
  ADD INDEX `idx_user_id`(`userId`) USING BTREE;

-- 4.4 规格项按规格查、秒杀按商品/时间段查、用户按手机号查
ALTER TABLE bz_specification_option ADD INDEX `idx_spec_id`(`specId`) USING BTREE;
ALTER TABLE bz_seckill_goods
  ADD INDEX `idx_goods_id`(`goodsId`) USING BTREE,
  ADD INDEX `idx_start_end`(`startTime`, `endTime`) USING BTREE;
ALTER TABLE bz_shopping_user ADD INDEX `idx_phone`(`phone`) USING BTREE;

-- 4.5 行政区划表补主键 + 父级索引（原表完全无索引，省市联动全表扫）
ALTER TABLE bz_area
  MODIFY `id` varchar(20) NOT NULL COMMENT '区域ID',
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD INDEX `idx_city_id`(`cityId`) USING BTREE;
ALTER TABLE bz_city
  MODIFY `id` varchar(20) NOT NULL COMMENT '城市ID',
  ADD PRIMARY KEY (`id`) USING BTREE,
  ADD INDEX `idx_province_id`(`provinceid`) USING BTREE;

-- ---------- 5. 字符集统一为 utf8mb4（支持 emoji，统一排序规则） ----------
ALTER TABLE bz_address CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_admin CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_admin_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_area CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_brand CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_cart_goods CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_city CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_goods CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_goods_image CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_goods_specification_option CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_orders CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_payment CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_permission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_product_type CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_province CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_role CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_role_permission CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_seckill_goods CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_shopping_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_specification CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE bz_specification_option CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
