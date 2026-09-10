<template>
  <div>
    <!-- 筛选区 -->
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
          placeholder="搜索题干关键词"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="题干" min-width="280">
          <template #default="{ row }">
            <span class="stem" :title="row.stem">{{ truncate(row.stem, 45) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ categoryNameMap[row.categoryId] || '-' }}</template>
        </el-table-column>
        <el-table-column label="题型" width="80">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ QUESTION_TYPE_TEXT[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="DIFFICULTY_TAG[row.difficulty]">{{ DIFFICULTY_TEXT[row.difficulty] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="浏览" width="80">
          <template #default="{ row }">
            <span class="view-count"><el-icon><View /></el-icon>{{ row.viewCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openDetail(row.id)">查看</el-button>
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

    <!-- 题目详情弹窗 -->
    <el-dialog v-model="detailVisible" title="题目详情" width="640px">
      <template v-if="detail">
        <div class="detail-stem text-pre-wrap">{{ detail.stem }}</div>
        <div class="detail-tags">
          <el-tag size="small" :type="DIFFICULTY_TAG[detail.difficulty]">{{ DIFFICULTY_TEXT[detail.difficulty] }}</el-tag>
          <el-tag size="small" type="info">{{ QUESTION_TYPE_TEXT[detail.type] }}</el-tag>
          <el-tag size="small">{{ categoryNameMap[detail.categoryId] || '未分类' }}</el-tag>
        </div>
        <ul class="options">
          <li v-for="(opt, i) in detail.options" :key="i" class="option">{{ opt }}</li>
        </ul>
        <el-collapse>
          <el-collapse-item title="查看答案与解析">
            <div class="answer-line">正确答案：<b>{{ detail.answer }}</b></div>
            <div class="analysis text-pre-wrap">{{ detail.analysis || '暂无解析' }}</div>
          </el-collapse-item>
        </el-collapse>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getQuestionPage, getQuestion } from '@/api/question'
import { getCategoryTree } from '@/api/category'
import { DIFFICULTY_TEXT, DIFFICULTY_TAG, QUESTION_TYPE_TEXT } from '@/utils/constants'
import { truncate } from '@/utils/format'

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

const detailVisible = ref(false)
const detail = ref(null)

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

async function openDetail(id) {
  detail.value = await getQuestion(id)
  detailVisible.value = true
}
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.stem {
  cursor: default;
}

.view-count {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  color: #909399;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.detail-stem {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 10px;
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.options {
  list-style: none;
  padding: 0;
  margin: 0 0 8px;
}

.option {
  padding: 6px 0;
  font-size: 14px;
}

.answer-line {
  margin-bottom: 6px;
}

.analysis {
  color: #606266;
}
</style>
