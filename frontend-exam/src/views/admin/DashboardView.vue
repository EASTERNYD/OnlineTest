<template>
  <div v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="s in statCards" :key="s.label" class="stat-tile">
        <div class="stat-label">{{ s.label }}</div>
        <div class="stat-value">{{ s.value.toLocaleString() }}</div>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 题目难度分布 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="viz-card">
          <template #header>
            <div class="viz-title">题目难度分布</div>
          </template>
          <div class="viz-root">
            <div v-if="difficultyDist.length" class="bar-list">
              <div v-for="d in difficultyDist" :key="d.difficulty" class="bar-row" :title="`${DIFFICULTY_TEXT[d.difficulty]}：${d.count} 题`">
                <span class="bar-label">{{ DIFFICULTY_TEXT[d.difficulty] }}</span>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: barWidth(d.count) }" />
                </div>
                <span class="bar-value">{{ d.count }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>

      <!-- 各分类题目数 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="viz-card">
          <template #header>
            <div class="viz-title">各分类题目数</div>
          </template>
          <div class="viz-root">
            <div v-if="categoryDist.length" class="bar-list">
              <div v-for="c in categoryDist" :key="c.categoryId" class="bar-row" :title="`${c.name || '未分类'}：${c.count} 题`">
                <span class="bar-label">{{ c.name || '未分类' }}</span>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: barWidth(c.count) }" />
                </div>
                <span class="bar-value">{{ c.count }}</span>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboardStats } from '@/api/stats'
import { DIFFICULTY_TEXT } from '@/utils/constants'

const stats = ref(null)
const loading = ref(false)

const statCards = computed(() => [
  { label: '题目总数', value: stats.value?.totalQuestions || 0 },
  { label: '试卷总数', value: stats.value?.totalExams || 0 },
  { label: '已发布试卷', value: stats.value?.publishedExams || 0 },
  { label: '用户总数', value: stats.value?.totalUsers || 0 },
  { label: '学生人数', value: stats.value?.totalStudents || 0 },
  { label: '考试记录', value: stats.value?.totalRecords || 0 },
  { label: '分类数量', value: stats.value?.totalCategories || 0 }
])

const difficultyDist = computed(() => stats.value?.difficultyDistribution || [])
const categoryDist = computed(
  () => [...(stats.value?.categoryDistribution || [])].sort((a, b) => b.count - a.count)
)

const maxCount = computed(() => {
  const all = [...difficultyDist.value, ...categoryDist.value].map((d) => d.count || 0)
  return Math.max(1, ...all)
})

function barWidth(count) {
  return `${Math.round(((count || 0) / maxCount.value) * 100)}%`
}

onMounted(async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* 图表色板：单色序贯（Element Plus primary-dark-2 #337ecc，白面 3:1 对比度校验通过）
   单序列柱形列表无需图例，标签 + 数值直接标注（relief），文字一律用墨色 token */
.viz-root {
  color-scheme: light;
  --bar-fill: #337ecc;
  --text-primary: #303133;
  --text-muted: #909399;
  --bar-track: #f0f2f5;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat-tile {
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 16px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--text-primary);
}

.viz-card {
  height: 100%;
}

.viz-title {
  font-weight: 600;
  font-size: 14px;
}

.bar-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-label {
  width: 56px;
  flex-shrink: 0;
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar-track {
  flex: 1;
  height: 16px;
  background: var(--bar-track);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  min-width: 4px;
  background: var(--bar-fill);
  border-radius: 0 4px 4px 0;
  transition: width 0.4s ease;
}

.bar-row:hover .bar-fill {
  filter: brightness(0.9);
}

.bar-value {
  width: 40px;
  flex-shrink: 0;
  text-align: right;
  font-size: 13px;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}
</style>
