<template>
  <div>
    <el-card shadow="never">
      <div class="table-toolbar">
        <div>
          <el-button type="primary" @click="openManualDialog">
            <el-icon><Plus /></el-icon>手动组卷
          </el-button>
          <el-button type="warning" plain @click="openComposeDialog">
            <el-icon><MagicStick /></el-icon>智能组卷
          </el-button>
        </div>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="试卷名称" min-width="200" />
        <el-table-column label="总分" width="80">
          <template #default="{ row }">{{ row.totalScore }}</template>
        </el-table-column>
        <el-table-column label="时长" width="90">
          <template #default="{ row }">{{ row.duration }} 分钟</template>
        </el-table-column>
        <el-table-column label="题数" width="70">
          <template #default="{ row }">{{ parseIds(row.questionIds).length }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="EXAM_STATUS_TAG[row.status]">{{ EXAM_STATUS_TEXT[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              text
              @click="handleStatus(row, 1)"
            >发布</el-button>
            <el-button
              v-else-if="row.status === 1"
              size="small"
              type="warning"
              text
              @click="handleStatus(row, 2)"
            >停止</el-button>
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
        @size-change="fetchList"
      />
    </el-card>

    <!-- 手动组卷弹窗 -->
    <el-dialog v-model="manualVisible" title="手动组卷" width="900px" top="4vh">
      <el-form :model="manualForm" label-width="80px" class="mb16">
        <el-row :gutter="12">
          <el-col :span="10">
            <el-form-item label="试卷名称">
              <el-input v-model="manualForm.name" placeholder="试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="总分">
              <el-input-number v-model="manualForm.totalScore" :min="1" :max="1000" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="时长(分)">
              <el-input-number v-model="manualForm.duration" :min="1" :max="600" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 选题区 -->
      <div class="picker">
        <div class="picker-filters">
          <el-tree-select
            v-model="pickerQuery.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="分类"
            clearable
            check-strictly
            style="width: 150px"
          />
          <el-select v-model="pickerQuery.difficulty" placeholder="难度" clearable style="width: 110px">
            <el-option v-for="(text, val) in DIFFICULTY_TEXT" :key="val" :label="text" :value="Number(val)" />
          </el-select>
          <el-input
            v-model="pickerQuery.keyword"
            placeholder="题干关键词"
            clearable
            style="width: 180px"
            @keyup.enter="fetchPicker"
          />
          <el-button type="primary" @click="fetchPicker">筛选</el-button>
          <span class="picked-count">已选 {{ manualForm.questionIds.length }} 题</span>
        </div>
        <el-table
          ref="pickerTableRef"
          v-loading="pickerLoading"
          :data="pickerList"
          stripe
          max-height="360"
          @selection-change="onPickerSelect"
        >
          <el-table-column type="selection" width="44" />
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="题干" min-width="280">
            <template #default="{ row }">
              <span :title="row.stem">{{ truncate(row.stem, 42) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="题型" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.type === 1 ? 'warning' : 'primary'">{{ QUESTION_TYPE_TEXT[row.type] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="难度" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="DIFFICULTY_TAG[row.difficulty]">{{ DIFFICULTY_TEXT[row.difficulty] }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          background
          layout="total, prev, pager, next"
          :total="pickerTotal"
          :page-size="10"
          v-model:current-page="pickerPage"
          @current-change="fetchPicker"
        />
      </div>
      <template #footer>
        <el-button @click="manualVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleManualSave">保存试卷（草稿）</el-button>
      </template>
    </el-dialog>

    <!-- 智能组卷弹窗 -->
    <el-dialog v-model="composeVisible" title="智能组卷（按规则抽题）" width="720px">
      <el-form label-width="90px">
        <el-form-item label="试卷名称">
          <el-input v-model="composeForm.name" placeholder="试卷名称" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="composeForm.duration" :min="1" :max="600" />
        </el-form-item>
        <el-form-item label="抽题规则">
          <div class="rules">
            <div v-for="(r, i) in composeForm.rules" :key="i" class="rule-row">
              <el-tree-select
                v-model="r.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="分类"
                check-strictly
                style="width: 180px"
              />
              <el-input-number v-model="r.count" :min="1" :max="100" placeholder="数量" />
              <el-select v-model="r.difficulty" placeholder="难度不限" clearable style="width: 120px">
                <el-option v-for="(text, val) in DIFFICULTY_TEXT" :key="val" :label="text" :value="Number(val)" />
              </el-select>
              <el-button
                :disabled="composeForm.rules.length <= 1"
                text
                type="danger"
                @click="composeForm.rules.splice(i, 1)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button text type="primary" @click="addRule">
              <el-icon><Plus /></el-icon>添加规则
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="每条规则按「分类 + 难度 + 数量」随机抽题；指定难度抽不足时自动放宽难度补齐。生成后为草稿状态，需点击「发布」后才能开考。"
      />
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" :loading="composing" @click="handleCompose">生成试卷</el-button>
      </template>
    </el-dialog>

    <!-- 试卷详情弹窗（含答案） -->
    <el-dialog v-model="detailVisible" :title="detail?.name || '试卷详情'" width="760px" top="5vh">
      <template v-if="detail">
        <div class="detail-meta">
          <el-tag size="small" :type="EXAM_STATUS_TAG[detail.status]">{{ EXAM_STATUS_TEXT[detail.status] }}</el-tag>
          <span>总分 {{ detail.totalScore }}</span>
          <span>时长 {{ detail.duration }} 分钟</span>
          <span>共 {{ detail.questions?.length || 0 }} 题</span>
        </div>
        <div v-for="(q, i) in detail.questions" :key="q.id" class="q-item">
          <div class="q-stem">{{ i + 1 }}. {{ q.stem }}</div>
          <div class="q-opts">
            <div v-for="(opt, oi) in q.options" :key="oi" class="q-opt">{{ opt }}</div>
          </div>
          <div class="q-answer">
            答案：<b>{{ q.answer }}</b>
            <span v-if="q.analysis" class="q-analysis">｜解析：{{ q.analysis }}</span>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getExamPage,
  getExamDetail,
  createExam,
  composeExam,
  updateExamStatus,
  deleteExam
} from '@/api/exam'
import { getQuestionPage } from '@/api/question'
import { getCategoryTree } from '@/api/category'
import {
  EXAM_STATUS_TEXT,
  EXAM_STATUS_TAG,
  DIFFICULTY_TEXT,
  DIFFICULTY_TAG,
  QUESTION_TYPE_TEXT
} from '@/utils/constants'
import { formatTime, truncate } from '@/utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })
const categoryTree = ref([])

const saving = ref(false)
const composing = ref(false)

// ===== 手动组卷 =====
const manualVisible = ref(false)
const manualForm = reactive({
  name: '',
  totalScore: 100,
  duration: 30,
  questionIds: []
})
const pickerList = ref([])
const pickerTotal = ref(0)
const pickerLoading = ref(false)
const pickerPage = ref(1)
const pickerQuery = reactive({ categoryId: null, difficulty: null, keyword: '' })
const pickerTableRef = ref(null)

// ===== 智能组卷 =====
const composeVisible = ref(false)
const composeForm = reactive({
  name: '',
  duration: 30,
  rules: [{ categoryId: null, count: 1, difficulty: null }]
})

// ===== 详情 =====
const detailVisible = ref(false)
const detail = ref(null)

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

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  fetchList()
})

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

async function openDetail(row) {
  detail.value = await getExamDetail(row.id)
  detailVisible.value = true
}

async function handleStatus(row, status) {
  const action = status === 1 ? '发布' : '停止'
  await ElMessageBox.confirm(
    `确定${action}试卷「${row.name}」吗？${status === 1 ? '发布后学生即可参加考试。' : '停止后学生将无法再开考。'}`,
    '提示',
    { type: 'warning' }
  )
  await updateExamStatus(row.id, status)
  row.status = status
  ElMessage.success(`已${action}`)
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除试卷「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteExam(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

// ===== 手动组卷 =====
function openManualDialog() {
  Object.assign(manualForm, {
    name: '',
    totalScore: 100,
    duration: 30,
    questionIds: []
  })
  pickerPage.value = 1
  pickerList.value = []
  manualVisible.value = true
  fetchPicker()
}

async function fetchPicker() {
  pickerLoading.value = true
  try {
    const data = await getQuestionPage({
      page: pickerPage.value,
      size: 10,
      categoryId: pickerQuery.categoryId ?? undefined,
      difficulty: pickerQuery.difficulty ?? undefined,
      keyword: pickerQuery.keyword || undefined
    })
    pickerList.value = data.list
    pickerTotal.value = data.total
  } finally {
    pickerLoading.value = false
  }
}

function onPickerSelect(rows) {
  manualForm.questionIds = rows.map((r) => r.id)
}

async function handleManualSave() {
  if (!manualForm.name.trim()) {
    ElMessage.warning('请输入试卷名称')
    return
  }
  if (!manualForm.questionIds.length) {
    ElMessage.warning('请至少勾选 1 道题目')
    return
  }
  saving.value = true
  try {
    await createExam({
      name: manualForm.name.trim(),
      totalScore: manualForm.totalScore,
      duration: manualForm.duration,
      questionIds: manualForm.questionIds
    })
    ElMessage.success('试卷创建成功（草稿状态），发布后学生可参加')
    manualVisible.value = false
    fetchList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

// ===== 智能组卷 =====
function openComposeDialog() {
  Object.assign(composeForm, {
    name: '',
    duration: 30,
    rules: [{ categoryId: null, count: 1, difficulty: null }]
  })
  composeVisible.value = true
}

function addRule() {
  composeForm.rules.push({ categoryId: null, count: 1, difficulty: null })
}

async function handleCompose() {
  if (!composeForm.name.trim()) {
    ElMessage.warning('请输入试卷名称')
    return
  }
  for (const r of composeForm.rules) {
    if (!r.categoryId || !r.count) {
      ElMessage.warning('请完整填写每条抽题规则（分类 + 数量）')
      return
    }
  }
  composing.value = true
  try {
    const id = await composeExam({
      name: composeForm.name.trim(),
      duration: composeForm.duration,
      rules: composeForm.rules.map((r) => ({
        categoryId: r.categoryId,
        count: r.count,
        difficulty: r.difficulty ?? undefined
      }))
    })
    ElMessage.success(`组卷成功，新试卷 ID：${id}（草稿状态），请发布后开考`)
    composeVisible.value = false
    fetchList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    composing.value = false
  }
}
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.picker-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.picked-count {
  margin-left: auto;
  color: var(--el-color-primary);
  font-size: 13px;
}

.rules {
  width: 100%;
}

.rule-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #606266;
  font-size: 13px;
  margin-bottom: 12px;
}

.q-item {
  padding: 10px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.q-stem {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
}

.q-opts {
  margin-bottom: 4px;
}

.q-opt {
  font-size: 13px;
  color: #606266;
  padding: 1px 0;
}

.q-answer {
  font-size: 13px;
  color: #303133;
}

.q-analysis {
  color: #909399;
}
</style>
