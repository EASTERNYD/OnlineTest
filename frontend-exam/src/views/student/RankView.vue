<template>
  <el-card shadow="never">
    <template #header>
      <div class="header">
        <span class="header-title">成绩排行榜</span>
        <el-select
          v-model="examFilter"
          placeholder="按试卷筛选"
          clearable
          style="width: 220px"
          @change="applyFilter"
        >
          <el-option v-for="e in examOptions" :key="e" :label="e" :value="e" />
        </el-select>
      </div>
    </template>

    <el-table v-loading="loading" :data="displayList" stripe>
      <el-table-column label="排名" width="90">
        <template #default="{ $index }">
          <span v-if="$index === 0" class="medal gold">🥇 1</span>
          <span v-else-if="$index === 1" class="medal silver">🥈 2</span>
          <span v-else-if="$index === 2" class="medal bronze">🥉 3</span>
          <span v-else class="rank-no">{{ $index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="140">
        <template #default="{ row }">
          <span class="nickname">{{ row.nickname }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="examName" label="试卷" min-width="180" />
      <el-table-column label="得分" width="100">
        <template #default="{ row }">
          <span class="score">{{ row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="correctCount" label="正确题数" width="90" />
      <el-table-column label="交卷时间" width="170">
        <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !displayList.length" description="暂无排行数据" />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRanking } from '@/api/record'
import { formatTime } from '@/utils/format'

const list = ref([])
const loading = ref(false)
const examFilter = ref('')

const examOptions = computed(() =>
  [...new Set(list.value.map((r) => r.examName).filter(Boolean))]
)

// 后端 ranking 无筛选参数，前端按试卷名过滤后重排
const displayList = computed(() => {
  if (!examFilter.value) return list.value
  return list.value.filter((r) => r.examName === examFilter.value)
})

function applyFilter() {
  // 占位：displayList 由 computed 自动响应
}

onMounted(async () => {
  loading.value = true
  try {
    list.value = await getRanking()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: 600;
}

.medal {
  font-size: 14px;
  font-weight: 600;
}

.rank-no {
  color: #909399;
}

.nickname {
  font-weight: 500;
}

.score {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-color-primary);
}
</style>
