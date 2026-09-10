<template>
  <div>
    <el-card shadow="never">
      <div class="table-toolbar">
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增轮播图
        </el-button>
      </div>
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="图片" width="140">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" fit="cover" class="thumb" :preview-src-list="[row.imageUrl]" preview-teleported />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" />
        <el-table-column prop="linkUrl" label="跳转链接" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑轮播图' : '新增轮播图'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="轮播图标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <UploadImage v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="https://…（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getBannerPage, createBanner, updateBanner, updateBannerStatus, deleteBanner } from '@/api/banner'
import UploadImage from '@/components/UploadImage.vue'
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
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const data = await getBannerPage({ page: query.page, size: query.size })
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
    imageUrl: row?.imageUrl ?? '',
    linkUrl: row?.linkUrl ?? '',
    sortOrder: row?.sortOrder ?? 0,
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
      imageUrl: form.imageUrl,
      linkUrl: form.linkUrl,
      sortOrder: form.sortOrder,
      status: form.status
    }
    if (form.id) {
      await updateBanner(form.id, payload)
    } else {
      await createBanner(payload)
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
  await updateBannerStatus(row.id, v ? 1 : 0)
  row.status = v ? 1 : 0
  ElMessage.success(v ? '已启用' : '已禁用')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除轮播图「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.thumb {
  width: 110px;
  height: 60px;
  border-radius: 4px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
