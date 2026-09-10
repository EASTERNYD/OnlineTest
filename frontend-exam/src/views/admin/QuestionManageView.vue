<template>
  <div>
    <el-card shadow="never" class="mb16">
      <div class="filters">
        <el-tree-select
          v-model="query.categoryId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="全部分类"
          clearable
          check-strictly
          style="width: 180px"
        />
        <el-select v-model="query.difficulty" placeholder="难度" clearable style="width: 120px">
          <el-option v-for="(text, val) in DIFFICULTY_TEXT" :key="val" :label="text" :value="Number(val)" />
        </el-select>
        <el-select v-model="query.type" placeholder="题型" clearable style="width: 120px">
          <el-option v-for="(text, val) in QUESTION_TYPE_TEXT" :key="val" :label="text" :value="Number(val)" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="题干关键词"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <div class="spacer" />
        <el-button @click="router.push('/admin/question/import')">
          <el-icon><Download /></el-icon>导入题目
        </el-button>
        <el-button type="warning" plain @click="openAiDialog">
          <el-icon><MagicStick /></el-icon>AI 生成
        </el-button>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增题目
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题干" min-width="260">
          <template #default="{ row }">
            <span class="stem" :title="row.stem">{{ truncate(row.stem, 40) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ categoryNameMap[row.categoryId] || '-' }}</template>
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
        <el-table-column label="答案" width="70">
          <template #default="{ row }">
            <b>{{ row.answer }}</b>
          </template>
        </el-table-column>
        <el-table-column label="浏览" width="80">
          <template #default="{ row }">{{ row.viewCount }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openDialog(row)">编辑</el-button>
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

    <!-- 新增/编辑题目弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题目' : '新增题目'" width="720px" top="5vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type" :disabled="!!form.id">
            <el-radio-button :value="0">单选</el-radio-button>
            <el-radio-button :value="1">多选</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="选择分类"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="form.difficulty">
            <el-radio-button v-for="(text, val) in DIFFICULTY_TEXT" :key="val" :value="Number(val)">
              {{ text }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题干" prop="stem">
          <el-input v-model="form.stem" type="textarea" :rows="3" placeholder="题干内容" />
        </el-form-item>
        <el-form-item label="选项" prop="optionTexts">
          <div class="options-editor">
            <div v-for="(opt, i) in form.optionTexts" :key="i" class="option-row">
              <span class="option-letter">{{ indexToLetter(i) }}.</span>
              <el-input v-model="form.optionTexts[i]" :placeholder="`选项 ${indexToLetter(i)}`" />
              <el-button
                :disabled="form.optionTexts.length <= 2"
                text
                type="danger"
                @click="form.optionTexts.splice(i, 1)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button :disabled="form.optionTexts.length >= 8" text type="primary" @click="addOption">
              <el-icon><Plus /></el-icon>添加选项
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <!-- 单选 -->
          <el-radio-group v-if="form.type === 0" v-model="form.answer">
            <el-radio v-for="i in form.optionTexts.length" :key="i" :value="indexToLetter(i - 1)">
              {{ indexToLetter(i - 1) }}
            </el-radio>
          </el-radio-group>
          <!-- 多选：答案字母排序后拼接 -->
          <el-checkbox-group v-else v-model="form.answerLetters">
            <el-checkbox v-for="i in form.optionTexts.length" :key="i" :value="indexToLetter(i - 1)">
              {{ indexToLetter(i - 1) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="3" placeholder="答案解析（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI 生成题目弹窗 -->
    <el-dialog v-model="aiVisible" title="AI 生成题目" width="760px" top="5vh">
      <div class="ai-form">
        <el-input
          v-model="aiForm.knowledgePoint"
          type="textarea"
          :rows="2"
          placeholder="输入知识点 / 出题指令，如：生成 5 道关于牛顿力学的单选题"
        />
        <div class="ai-form-row">
          <span>数量</span>
          <el-input-number v-model="aiForm.count" :min="1" :max="20" />
          <span>难度</span>
          <el-select v-model="aiForm.difficulty" style="width: 110px">
            <el-option v-for="(text, val) in DIFFICULTY_TEXT" :key="val" :label="text" :value="Number(val)" />
          </el-select>
          <span>分类</span>
          <el-tree-select
            v-model="aiForm.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="可选"
            clearable
            check-strictly
            style="width: 170px"
          />
        </div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          title="AI 生成调用 DeepSeek 大模型，可能需要 1-2 分钟，请耐心等待；后端需配置 llm.api-key 才可用。"
        />
      </div>
      <el-table
        v-if="aiResults.length"
        :data="aiResults"
        class="ai-table"
        max-height="320"
        @selection-change="(rows) => (aiSelected = rows)"
      >
        <el-table-column type="selection" width="44" />
        <el-table-column label="题干" min-width="240">
          <template #default="{ row }">
            <span :title="row.stem">{{ truncate(row.stem, 36) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="答案" width="60">
          <template #default="{ row }"><b>{{ row.answer }}</b></template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="DIFFICULTY_TAG[row.difficulty]">{{ DIFFICULTY_TEXT[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="aiVisible = false">关闭</el-button>
        <el-button type="primary" :loading="aiGenerating" @click="handleAiGenerate">生成题目</el-button>
        <el-button
          type="success"
          :disabled="!aiSelected.length"
          :loading="aiSaving"
          @click="handleAiSave"
        >
          保存选中（{{ aiSelected.length }}）
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getQuestionPage,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  aiGenerateQuestion
} from '@/api/question'
import { getCategoryTree } from '@/api/category'
import { DIFFICULTY_TEXT, DIFFICULTY_TAG, QUESTION_TYPE_TEXT } from '@/utils/constants'
import { truncate } from '@/utils/format'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const categoryTree = ref([])
const categoryNameMap = reactive({})

const query = reactive({
  page: 1,
  size: 10,
  categoryId: null,
  difficulty: null,
  type: null,
  keyword: ''
})

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  type: 0,
  categoryId: null,
  difficulty: 1,
  stem: '',
  optionTexts: ['', '', '', ''],
  answer: '',
  answerLetters: [],
  analysis: ''
})

const rules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  stem: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请选择正确答案', trigger: 'change' }]
}

// ===== AI 生成 =====
const aiVisible = ref(false)
const aiGenerating = ref(false)
const aiSaving = ref(false)
const aiResults = ref([])
const aiSelected = ref([])
const aiForm = reactive({
  knowledgePoint: '',
  count: 5,
  difficulty: 1,
  categoryId: null
})

function buildCategoryMap(nodes) {
  for (const n of nodes || []) {
    categoryNameMap[n.id] = n.name
    if (n.children?.length) buildCategoryMap(n.children)
  }
}

onMounted(async () => {
  categoryTree.value = await getCategoryTree()
  buildCategoryMap(categoryTree.value)
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getQuestionPage({
      page: query.page,
      size: query.size,
      categoryId: query.categoryId ?? undefined,
      difficulty: query.difficulty ?? undefined,
      type: query.type ?? undefined,
      keyword: query.keyword || undefined
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
  query.categoryId = null
  query.difficulty = null
  query.type = null
  query.keyword = ''
  handleSearch()
}

// ===== 新增/编辑 =====
const indexToLetter = (i) => String.fromCharCode(65 + i)

function addOption() {
  if (form.optionTexts.length < 8) form.optionTexts.push('')
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id ?? null,
    type: row?.type ?? 0,
    categoryId: row?.categoryId ?? null,
    difficulty: row?.difficulty ?? 1,
    stem: row?.stem ?? '',
    // 响应里 options 是数组 ["A. xx", ...]，编辑时直接回填完整文本
    optionTexts: row?.options?.length ? [...row.options] : ['', '', '', ''],
    answer: '',
    answerLetters: [],
    analysis: row?.analysis ?? ''
  })
  if (row?.answer) {
    if (form.type === 1) {
      form.answerLetters = row.answer.split('')
    } else {
      form.answer = row.answer
    }
  }
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  // 过滤空选项，选项文本需带字母前缀
  const options = form.optionTexts
    .map((t, i) => `${indexToLetter(i)}. ${t.trim()}`)
    .filter((t) => t.trim().length > 3)
  if (options.length < 2) {
    ElMessage.warning('请至少填写 2 个选项')
    return
  }
  const answer =
    form.type === 1 ? [...form.answerLetters].sort().join('') : form.answer
  if (!answer) {
    ElMessage.warning('请选择正确答案')
    return
  }
  saving.value = true
  try {
    // 注意：请求体 options 必须是 JSON 字符串
    const payload = {
      type: form.type,
      categoryId: form.categoryId,
      difficulty: form.difficulty,
      stem: form.stem,
      options: JSON.stringify(options),
      answer,
      analysis: form.analysis
    }
    if (form.id) {
      await updateQuestion(form.id, payload)
    } else {
      await createQuestion(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    // 错误提示由拦截器处理
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除这道题目吗？删除后不可恢复。', '提示', { type: 'warning' })
  await deleteQuestion(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

// ===== AI 生成 =====
function openAiDialog() {
  aiResults.value = []
  aiSelected.value = []
  aiVisible.value = true
}

async function handleAiGenerate() {
  if (!aiForm.knowledgePoint.trim()) {
    ElMessage.warning('请输入知识点 / 出题指令')
    return
  }
  aiGenerating.value = true
  aiResults.value = []
  aiSelected.value = []
  try {
    aiResults.value = await aiGenerateQuestion({
      knowledgePoint: aiForm.knowledgePoint.trim(),
      count: aiForm.count,
      difficulty: aiForm.difficulty,
      categoryId: aiForm.categoryId ?? undefined
    })
    ElMessage.success(`生成成功，共 ${aiResults.value.length} 道，勾选后保存`)
  } catch (e) {
    // 拦截器已提示（如未配置 llm.api-key）
  } finally {
    aiGenerating.value = false
  }
}

async function handleAiSave() {
  aiSaving.value = true
  try {
    for (const q of aiSelected.value) {
      await createQuestion({
        type: 0,
        categoryId: q.categoryId ?? aiForm.categoryId,
        difficulty: q.difficulty ?? aiForm.difficulty,
        stem: q.stem,
        options: JSON.stringify(q.options),
        answer: q.answer,
        analysis: q.analysis || ''
      })
    }
    ElMessage.success(`已保存 ${aiSelected.value.length} 道题目`)
    aiVisible.value = false
    fetchList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    aiSaving.value = false
  }
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.spacer {
  flex: 1;
}

.stem {
  cursor: default;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.option-letter {
  width: 20px;
  font-weight: 600;
  flex-shrink: 0;
}

.ai-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.ai-form-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  color: #606266;
  font-size: 13px;
}

.ai-table {
  margin-top: 4px;
}
</style>
