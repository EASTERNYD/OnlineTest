<template>
  <div>
    <el-card shadow="never">
      <div class="table-toolbar">
        <div class="left">
          <el-radio-group v-model="viewMode">
            <el-radio-button value="list">列表视图</el-radio-button>
            <el-radio-button value="tree">树形视图</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增分类
        </el-button>
      </div>

      <!-- 列表视图 -->
      <el-table v-if="viewMode === 'list'" v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="分类名" min-width="150" />
        <el-table-column label="父级分类" min-width="130">
          <template #default="{ row }">{{ parentName(row.parentId) }}</template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数" width="90" />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" text @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 树形视图 -->
      <el-tree
        v-else
        v-loading="loading"
        :data="tree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        default-expand-all
        class="tree"
      >
        <template #default="{ data }">
          <div class="tree-node">
            <span>{{ data.name }}</span>
            <span v-if="data.questionCount != null" class="tree-count">{{ data.questionCount }} 题</span>
            <span class="tree-actions">
              <el-button size="small" type="primary" text @click="openDialog(data)">编辑</el-button>
              <el-button size="small" type="danger" text @click="handleDelete(data)">删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名" prop="name">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="form.parentId" placeholder="无（顶级分类）" clearable style="width: 100%">
            <el-option
              v-for="c in list.filter((x) => x.id !== form.id)"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { formatTime } from '@/utils/format'

const list = ref([])
const tree = ref([])
const loading = ref(false)
const viewMode = ref('list')

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  parentId: null,
  sortOrder: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }]
}

const nameMap = computed(() => {
  const map = {}
  for (const c of list.value) map[c.id] = c.name
  return map
})

function parentName(parentId) {
  if (!parentId) return '-'
  return nameMap.value[parentId] || `#${parentId}`
}

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const [l, t] = await Promise.all([getCategoryList(), getCategoryTree()])
    list.value = l
    tree.value = t
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id ?? null,
    name: row?.name ?? '',
    parentId: row?.parentId || null,
    sortOrder: row?.sortOrder ?? 0
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      name: form.name,
      parentId: form.parentId ?? 0,
      sortOrder: form.sortOrder
    }
    if (form.id) {
      await updateCategory(form.id, payload)
    } else {
      await createCategory(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    // 错误提示由拦截器处理
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确定删除分类「${row.name}」吗？${row.questionCount ? `该分类下有 ${row.questionCount} 道题目，删除将被拒绝。` : ''}`,
    '提示',
    { type: 'warning' }
  )
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
  } catch (e) {
    // 后端校验「被题目引用时删除失败」，错误信息已由拦截器展示
    return
  }
  fetchData()
}
</script>

<style scoped>
.left {
  display: flex;
  align-items: center;
}

.tree {
  background: transparent;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 8px;
}

.tree-count {
  font-size: 12px;
  color: #909399;
}

.tree-actions {
  margin-left: auto;
}
</style>
