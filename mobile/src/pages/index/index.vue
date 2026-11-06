<script setup>
import { ref } from 'vue'
import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { getGoodsPage } from '@/api/goods.js'

const list = ref([])
const page = ref(1)
const size = 10
const finished = ref(false)
const loading = ref(false)

async function load(reset = false) {
  if (loading.value) return
  if (reset) {
    page.value = 1
    finished.value = false
  }
  if (finished.value && !reset) return
  loading.value = true
  try {
    const data = await getGoodsPage({ current: page.value, size })
    const records = data.records || []
    list.value = reset ? records : [...list.value, ...records]
    finished.value = list.value >= data.total
    if (!reset) page.value += 1
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

onLoad(() => load(true))
onReachBottom(() => load())
onPullDownRefresh(() => load(true))

function goDetail(id) {
  uni.navigateTo({ url: `/pages/detail/detail?id=${id}` })
}
</script>

<template>
  <view class="page">
    <view v-for="item in list" :key="item.id" class="goods-card" @click="goDetail(item.id)">
      <image class="goods-img" :src="item.headerPic" mode="aspectFill" />
      <view class="goods-info">
        <text class="goods-name">{{ item.goodsName }}</text>
        <text class="goods-caption">{{ item.caption }}</text>
        <view class="goods-bottom">
          <text class="price">¥{{ item.costPrice || item.price }}</text>
          <text class="buy-btn">立即抢购</text>
        </view>
      </view>
    </view>

    <view class="footer">
      <text v-if="loading">加载中...</text>
      <text v-else-if="finished">没有更多了</text>
    </view>
  </view>
</template>

<style scoped>
.page {
  padding: 20rpx;
}
.goods-card {
  display: flex;
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.goods-img {
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
  background: #f0f2f5;
  flex-shrink: 0;
}
.goods-info {
  flex: 1;
  margin-left: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.goods-name {
  font-size: 30rpx;
  font-weight: 600;
}
.goods-caption {
  font-size: 24rpx;
  color: #909399;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  color: #f56c6c;
  font-size: 34rpx;
  font-weight: 700;
}
.buy-btn {
  background: linear-gradient(90deg, #ff6b6b, #f56c6c);
  color: #fff;
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border-radius: 28rpx;
}
.footer {
  text-align: center;
  color: #909399;
  font-size: 24rpx;
  padding: 24rpx 0;
}
</style>
