<template>
  <el-card shadow="never">
    <template #header>
      <span class="header-title">我的考试记录</span>
    </template>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="examName" label="试卷" min-width="180" />
      <el-table-column label="得分" width="100">
        <template #default="{ row }">
          <span class="score">{{ row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="correctCount" label="正确" width="70" />
      <el-table-column prop="wrongCount" label="错误" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
            {{ RECORD_STATUS_TEXT[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="交卷时间" width="170">
        <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" text @click="openDetail(row.id)">详情</el-button>
          <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !list.length" description="还没有考试记录，去参加一场考试吧" />
  </el-card>

  <!-- 成绩详情弹窗 -->
  <el-dialog v-model="detailVisible" title="成绩详情" width="720px">
    <template v-if="detail">
      <div class="summary">
        <div class="summary-item">
          <div class="label">试卷</div>
          <div class="value">{{ detail.examName }}</div>
        </div>
        <div class="summary-item">
          <div class="label">得分</div>
          <div class="value score">{{ detail.score }}</div>
        </div>
        <div class="summary-item">
          <div class="label">正确 / 错误</div>
          <div class="value">
            <span class="green">{{ detail.correctCount }}</span>
            /
            <span class="red">{{ detail.wrongCount }}</span>
          </div>
        </div>
        <div class="summary-item">
          <div class="label">交卷时间</div>
          <div class="value">{{ formatTime(detail.submitTime) }}</div>
        </div>
      </div>
      <el-divider content-position="left">逐题明细</el-divider>
      <div v-for="(a, i) in detail.answers" :key="a.questionId" class="answer-item">
        <div class="a-head">
          <span class="a-no">{{ i + 1 }}.</span>
          <span class="a-stem text-pre-wrap">{{ a.stem }}</span>
          <el-tag size="small" :type="a.correct ? 'success' : 'danger'">
            {{ a.correct ? '正确' : '错误' }}
          </el-tag>
        </div>
        <div class="a-line">你的答案：<b :class="a.correct ? 'green' : 'red'">{{ a.userAnswer || '未作答' }}</b></div>
        <div v-if="!a.correct" class="a-line">正确答案：<b class="green">{{ a.correctAnswer }}</b></div>
        <div v-if="a.analysis" class="a-line analysis text-pre-wrap">解析：{{ a.analysis }}</div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getRecordList, getRecordDetail, deleteRecord } from '@/api/record'
import { RECORD_STATUS_TEXT } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const route = useRoute()
const list = ref([])
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)

onMounted(async () => {
  await fetchList()
  // 交卷后跳转携带 ?detail=recordId，自动打开成绩详情
  if (route.query.detail) {
    openDetail(Number(route.query.detail))
  }
})

async function fetchList() {
  loading.value = true
  try {
    list.value = await getRecordList()
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  detail.value = await getRecordDetail(id)
  detailVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.examName}」的这条考试记录吗？`, '提示', { type: 'warning' })
  await deleteRecord(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.header-title {
  font-weight: 600;
}

.score {
  font-size: 16px;
  font-weight: 700;
  color: var(--el-color-primary);
}

.green {
  color: var(--el-color-success);
}

.red {
  color: var(--el-color-danger);
}

.summary {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.summary-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-item .value {
  font-size: 15px;
  font-weight: 600;
}

.answer-item {
  padding: 10px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.answer-item:last-child {
  border-bottom: none;
}

.a-head {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 6px;
}

.a-no {
  font-weight: 700;
  flex-shrink: 0;
}

.a-stem {
  flex: 1;
  font-size: 14px;
}

.a-line {
  font-size: 13px;
  margin-left: 22px;
  margin-bottom: 3px;
}

.analysis {
  color: #606266;
}
</style>
