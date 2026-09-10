<template>
  <el-card v-loading="loading" shadow="never">
    <template #header>
      <div class="header">
        <el-button text @click="router.back()">
          <el-icon><Back /></el-icon>返回
        </el-button>
      </div>
    </template>
    <div v-if="notice">
      <h2 class="title">{{ notice.title }}</h2>
      <div class="meta">
        <el-tag size="small" :type="NOTICE_TYPE_TAG[notice.type]">{{ NOTICE_TYPE_TEXT[notice.type] }}</el-tag>
        <span>发布时间：{{ formatTime(notice.createTime) }}</span>
      </div>
      <el-divider />
      <!-- 内容为 HTML（富文本编辑器产物）或纯文本，均可正常展示 -->
      <div class="content text-pre-wrap" v-html="notice.content"></div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNotice } from '@/api/notice'
import { NOTICE_TYPE_TEXT, NOTICE_TYPE_TAG } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const notice = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    notice.value = await getNotice(route.params.id)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.header {
  display: flex;
}

.title {
  margin: 0 0 12px;
  text-align: center;
}

.meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}

.content {
  padding: 8px 4px;
  font-size: 14px;
}
</style>
