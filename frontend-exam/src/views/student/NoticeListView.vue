<template>
  <el-card shadow="never">
    <template #header>
      <span class="header-title">公告列表</span>
    </template>
    <el-empty v-if="!loading && !notices.length" description="暂无公告" />
    <div v-for="n in notices" :key="n.id" class="notice-item" @click="router.push(`/notice/${n.id}`)">
      <el-tag size="small" :type="NOTICE_TYPE_TAG[n.type]">{{ NOTICE_TYPE_TEXT[n.type] }}</el-tag>
      <div class="notice-body">
        <div class="notice-title">{{ n.title }}</div>
        <div class="notice-time">{{ formatTime(n.createTime) }}</div>
      </div>
      <el-icon class="arrow"><ArrowRight /></el-icon>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEnabledNotices } from '@/api/notice'
import { NOTICE_TYPE_TEXT, NOTICE_TYPE_TAG } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const router = useRouter()
const notices = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    notices.value = await getEnabledNotices()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.header-title {
  font-weight: 600;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.notice-item:hover .notice-title {
  color: var(--el-color-primary);
}

.notice-body {
  flex: 1;
  min-width: 0;
}

.notice-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.arrow {
  color: #c0c4cc;
}
</style>
