<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <span class="header-title">考试列表</span>
        <span class="header-tip">仅展示已发布试卷，点击「开始考试」进入答题</span>
      </template>
      <el-table v-loading="loading" :data="publishedExams" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="试卷名称" min-width="200" />
        <el-table-column label="总分" width="90">
          <template #default="{ row }">{{ row.totalScore }}</template>
        </el-table-column>
        <el-table-column label="时长" width="90">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column label="题数" width="90">
          <template #default="{ row }">{{ parseIds(row.questionIds).length }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="router.push(`/exam/${row.id}/do`)">
              开始考试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !publishedExams.length" description="暂无已发布的考试" />
      <el-pagination
        v-if="total > query.size"
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="total"
        v-model:current-page="query.page"
        @current-change="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getExamPage } from '@/api/exam'
import { EXAM_STATUS } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })

// 学生端只展示已发布（status=1）的试卷
const publishedExams = computed(() => list.value.filter((e) => e.status === EXAM_STATUS.PUBLISHED))

// question_ids 可能已是数组或 JSON 字符串，兼容两种形态
function parseIds(ids) {
  if (Array.isArray(ids)) return ids
  if (typeof ids === 'string') {
    try {
      return JSON.parse(ids)
    } catch {
      return []
    }
  }
  return []
}

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const data = await getExamPage({ page: query.page, size: query.size })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.header-title {
  font-weight: 600;
  margin-right: 12px;
}

.header-tip {
  font-size: 12px;
  color: #909399;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
