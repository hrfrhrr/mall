<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getGoods } from '@/api/goods.js'
import { post } from '@/api/request.js'

const goods = ref(null)
const id = ref(null)

onLoad(async (query) => {
  id.value = query.id
  goods.value = await getGoods(query.id)
})

async function onBuy() {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  const orderId = await post('/order/create', { goodsId: id.value })
  uni.showToast({ title: '下单成功', icon: 'success' })
  console.log('[订单]', orderId)
}
</script>

<template>
  <view v-if="goods" class="page">
    <image class="banner" :src="goods.headerPic" mode="aspectFill" />
    <view class="card">
      <view class="price-row">
        <text class="price">¥{{ goods.price }}</text>
      </view>
      <text class="name">{{ goods.goodsName }}</text>
      <text class="caption">{{ goods.caption }}</text>
    </view>
    <view class="card intro">
      <text class="card-title">商品介绍</text>
      <rich-text :nodes="goods.introduction"></rich-text>
    </view>

    <view class="bottom-bar">
      <button class="buy" @click="onBuy">立即购买</button>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding-bottom: 140rpx;
}
.banner {
  width: 100%;
  height: 600rpx;
  background: #f0f2f5;
}
.card {
  background: #fff;
  margin: 20rpx;
  padding: 24rpx;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.card-title {
  font-weight: 600;
  margin-bottom: 12rpx;
}
.price {
  color: #f56c6c;
  font-size: 48rpx;
  font-weight: 700;
}
.name {
  font-size: 34rpx;
  font-weight: 600;
}
.caption {
  color: #909399;
  font-size: 26rpx;
}
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}
.buy {
  background: linear-gradient(90deg, #ff6b6b, #f56c6c);
  color: #fff;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: 600;
}
</style>
