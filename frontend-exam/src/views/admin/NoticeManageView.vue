<template>
  <div>
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增公告
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="NOTICE_TYPE_TAG[row.type]">{{ NOTICE_TYPE_TEXT[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :active-text="row.status === 1 ? '已发布' : '草稿'"
              inline-prompt
              @change="(v) => handleToggle(row, v)"
            />
          </template>
        </el-table-column>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '新增公告'" width="680px" top="6vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="1">通知</el-radio-button>
            <el-radio-button :value="2">警告</el-radio-button>
            <el-radio-button :value="3">活动</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <RichText v-model="form.content" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" inactive-text="草稿" inline-prompt />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticePage, createNotice, updateNotice, updateNoticeStatus, deleteNotice } from '@/api/notice'
import RichText from '@/components/RichText.vue'
import { NOTICE_TYPE_TEXT, NOTICE_TYPE_TAG } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10 })

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  title: '',
  content: '',
  type: 1,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'change' }]
}

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const data = await getNoticePage({ page: query.page, size: query.size })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id ?? null,
    title: row?.title ?? '',
    content: row?.content ?? '',
    type: row?.type ?? 1,
    status: row?.status ?? 1
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      type: form.type,
      status: form.status
    }
    if (form.id) {
      await updateNotice(form.id, payload)
    } else {
      await createNotice(payload)
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

async function handleToggle(row, v) {
  await updateNoticeStatus(row.id, v ? 1 : 0)
  row.status = v ? 1 : 0
  ElMessage.success(v ? '已发布' : '已转为草稿')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteNotice(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
