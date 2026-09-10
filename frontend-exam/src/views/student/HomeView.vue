<template>
  <div>
    <!-- 轮播图 -->
    <el-carousel v-if="banners.length" height="280px" class="banner mb16" :interval="4000">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <a :href="b.linkUrl || 'javascript:void(0)'" target="_blank" class="banner-link">
          <img :src="b.imageUrl" :alt="b.title" class="banner-img" />
        </a>
      </el-carousel-item>
    </el-carousel>
    <el-empty v-else description="暂无轮播图" :image-size="80" class="mb16" />

    <el-row :gutter="16">
      <!-- 最新公告 -->
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-button text type="primary" @click="router.push('/notice')">更多</el-button>
            </div>
          </template>
          <ul class="notice-list">
            <li v-for="n in notices" :key="n.id" class="notice-item" @click="router.push(`/notice/${n.id}`)">
              <el-tag size="small" :type="NOTICE_TYPE_TAG[n.type]" class="notice-tag">
                {{ NOTICE_TYPE_TEXT[n.type] }}
              </el-tag>
              <span class="notice-title">{{ n.title }}</span>
              <span class="notice-time">{{ formatDate(n.createTime) }}</span>
            </li>
          </ul>
          <el-empty v-if="!notices.length" description="暂无公告" :image-size="60" />
        </el-card>
      </el-col>

      <!-- 热门题目 -->
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>热门题目</span>
              <el-button text type="primary" @click="router.push('/question')">进入题库</el-button>
            </div>
          </template>
          <ul class="question-list">
            <li v-for="(q, i) in hotQuestions" :key="q.id" class="question-item">
              <span class="rank" :class="{ top3: i < 3 }">{{ i + 1 }}</span>
              <div class="q-main">
                <div class="q-stem" :title="q.stem">{{ truncate(q.stem, 50) }}</div>
                <div class="q-meta">
                  <el-tag size="small" :type="DIFFICULTY_TAG[q.difficulty]">{{ DIFFICULTY_TEXT[q.difficulty] }}</el-tag>
                  <el-tag size="small" type="info">{{ QUESTION_TYPE_TEXT[q.type] }}</el-tag>
                  <span class="view-count"><el-icon><View /></el-icon>{{ q.viewCount }}</span>
                </div>
              </div>
            </li>
          </ul>
          <el-empty v-if="!hotQuestions.length" description="暂无题目" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEnabledBanners } from '@/api/banner'
import { getLatestNotices } from '@/api/notice'
import { getHotQuestions } from '@/api/question'
import { NOTICE_TYPE_TEXT, NOTICE_TYPE_TAG, DIFFICULTY_TEXT, DIFFICULTY_TAG, QUESTION_TYPE_TEXT } from '@/utils/constants'
import { formatDate, truncate } from '@/utils/format'

const router = useRouter()
const banners = ref([])
const notices = ref([])
const hotQuestions = ref([])

onMounted(async () => {
  // 三个公开接口并行加载
  const [b, n, q] = await Promise.allSettled([
    getEnabledBanners(),
    getLatestNotices(5),
    getHotQuestions(10)
  ])
  banners.value = b.status === 'fulfilled' ? b.value : []
  notices.value = n.status === 'fulfilled' ? n.value : []
  hotQuestions.value = q.status === 'fulfilled' ? q.value : []
})
</script>

<style scoped>
.banner {
  border-radius: 8px;
  overflow: hidden;
}

.banner-link {
  display: block;
  width: 100%;
  height: 100%;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.notice-list,
.question-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
  cursor: pointer;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.notice-item:hover .notice-title {
  color: var(--el-color-primary);
}

.notice-time {
  font-size: 12px;
  color: #909399;
}

.question-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--el-border-color-lighter);
}

.question-item:last-child {
  border-bottom: none;
}

.rank {
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  border-radius: 4px;
  background: var(--el-fill-color);
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
  margin-top: 2px;
}

.rank.top3 {
  background: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
  font-weight: 600;
}

.q-main {
  flex: 1;
  min-width: 0;
}

.q-stem {
  font-size: 14px;
  margin-bottom: 6px;
}

.q-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-count {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}
</style>
