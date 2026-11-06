<script setup>
import { ref, onMounted } from 'vue'
import { getAds } from '@/api/goods'

const ads = ref([])

onMounted(async () => {
  const res = await getAds()
  ads.value = res.data || []
})
</script>

<template>
  <div>
    <el-card>
      <h2>工作台</h2>
      <p style="color: #606266">
        欢迎使用亿级高并发电商平台管理后台。基于 Spring Cloud Alibaba
        微服务架构，支持横向扩容与容器化部署。
      </p>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>首页广告位（Redis 缓存演示）</template>
      <el-row :gutter="16">
        <el-col v-for="ad in ads" :key="ad.id" :span="6" style="margin-bottom: 16px">
          <el-image :src="ad.pic" fit="cover" style="width: 100%; height: 120px; border-radius: 6px" />
          <div style="text-align: center; margin-top: 6px">{{ ad.title }}</div>
        </el-col>
      </el-row>
      <el-empty v-if="!ads.length" description="暂无广告数据" />
    </el-card>
  </div>
</template>
