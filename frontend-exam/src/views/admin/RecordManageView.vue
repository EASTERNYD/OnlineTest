<template>
  <div>
    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="filters">
          <el-input v-model="query.userId" placeholder="学生 ID" clearable style="width: 130px" />
          <el-input v-model="query.examId" placeholder="试卷 ID" clearable style="width: 130px" />
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="学生" min-width="130">
          <template #default="{ row }">
            <div class="student">
              <span class="nickname">{{ row.nickname }}</span>
              <span class="username">({{ row.username }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="examName" label="试卷" min-width="160" />
        <el-table-column label="得分" width="80">
          <template #default="{ row }">
            <b class="score">{{ row.score }}</b>
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
            <el-button size="small" type="primary" text @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50]"
        @current-change="fetchList"
        @size-change="handleSearch"
      />
    </el-card>

    <!-- 记录详情弹窗（与考生端一致结构） -->
    <el-dialog v-model="detailVisible" title="考试记录详情" width="720px">
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
          <div class="a-line">考生答案：<b :class="a.correct ? 'green' : 'red'">{{ a.userAnswer || '未作答' }}</b></div>
          <div v-if="!a.correct" class="a-line">正确答案：<b class="green">{{ a.correctAnswer }}</b></div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminRecordPage, getAdminRecordDetail, deleteAdminRecord } from '@/api/record'
import { RECORD_STATUS_TEXT } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, userId: '', examId: '' })

const detailVisible = ref(false)
const detail = ref(null)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const data = await getAdminRecordPage({
      page: query.page,
      size: query.size,
      userId: query.userId || undefined,
      examId: query.examId || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.userId = ''
  query.examId = ''
  handleSearch()
}

async function openDetail(row) {
  detail.value = await getAdminRecordDetail(row.id)
  detailVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确定删除 ${row.nickname}(${row.username}) 在「${row.examName}」中的考试记录吗？`,
    '提示',
    { type: 'warning' }
  )
  await deleteAdminRecord(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.nickname {
  font-weight: 500;
}

.username {
  font-size: 12px;
  color: #909399;
}

.score {
  color: var(--el-color-primary);
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
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
</style>
