<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGoodsPage, updateGoods } from '@/api/goods'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, goodsName: '' })

async function load() {
  loading.value = true
  try {
    const res = await getGoodsPage({ current: query.page, size: query.size, goodsName: query.goodsName })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  load()
}

function onPageChange(page) {
  query.page = page
  load()
}

async function onToggleMarketable(row) {
  await updateGoods({ id: row.id, isMarketable: row.isMarketable })
  ElMessage.success('已更新（缓存已失效）')
  load()
}

onMounted(load)
</script>

<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.goodsName" placeholder="商品名称" clearable style="width: 220px" @keyup.enter="onSearch" />
      <el-button type="primary" @click="onSearch">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="180" />
      <el-table-column label="头图" width="90">
        <template #default="{ row }">
          <el-image :src="row.headerPic" fit="cover" style="width: 48px; height: 48px; border-radius: 4px" />
        </template>
      </el-table-column>
      <el-table-column prop="goodsName" label="商品名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="caption" label="副标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="110">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="是否上架" width="140">
        <template #default="{ row }">
          <el-switch
            :model-value="row.isMarketable === 1"
            :active-value="1"
            :inactive-value="0"
            @change="(val) => { row.isMarketable = val; onToggleMarketable(row) }"
          />
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :current-page="query.page"
      :page-size="query.size"
      @current-change="onPageChange"
    />
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
