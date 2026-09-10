<template>
  <div class="exam-do" v-loading="starting">
    <!-- 顶部栏：试卷名 + 倒计时 + 交卷 -->
    <div class="top-bar">
      <div class="exam-name">
        <el-icon><Document /></el-icon>
        <span>{{ examStore.examName }}</span>
        <span class="progress-text">已答 {{ examStore.answeredCount }} / {{ examStore.totalCount }}</span>
      </div>
      <div class="right">
        <div class="countdown" :class="{ danger: examStore.timeLeft <= 300 }">
          <el-icon><Timer /></el-icon>
          <span>{{ secondsToClock(examStore.timeLeft) }}</span>
        </div>
        <el-button type="danger" :loading="submitting" @click="handleSubmit">提交试卷</el-button>
      </div>
    </div>

    <div class="exam-body">
      <!-- 题目区：全部滚动展示 -->
      <div ref="listRef" class="question-list">
        <el-card
          v-for="(q, i) in examStore.questions"
          :key="q.id"
          :ref="(el) => setCardRef(el, i)"
          shadow="never"
          class="question-card"
        >
          <div class="q-head">
            <span class="q-no">{{ i + 1 }}.</span>
            <el-tag size="small" :type="q.type === 1 ? 'warning' : 'primary'">
              {{ QUESTION_TYPE_TEXT[q.type] }}
            </el-tag>
            <el-tag v-if="q.difficulty" size="small" :type="DIFFICULTY_TAG[q.difficulty]">
              {{ DIFFICULTY_TEXT[q.difficulty] }}
            </el-tag>
          </div>
          <div class="q-stem text-pre-wrap">{{ q.stem }}</div>

          <!-- 单选 -->
          <el-radio-group
            v-if="q.type !== 1"
            :model-value="singleAnswer(q.id)"
            class="option-group"
            @update:model-value="(v) => (examStore.answerMap[q.id] = v)"
          >
            <el-radio v-for="(opt, oi) in q.options" :key="oi" :value="letter(opt)" class="option-item">
              {{ opt }}
            </el-radio>
          </el-radio-group>

          <!-- 多选：答案按字母排序后拼接提交 -->
          <el-checkbox-group
            v-else
            :model-value="multiAnswer(q.id)"
            class="option-group"
            @update:model-value="(v) => (examStore.answerMap[q.id] = [...v])"
          >
            <el-checkbox v-for="(opt, oi) in q.options" :key="oi" :value="letter(opt)" class="option-item">
              {{ opt }}
            </el-checkbox>
          </el-checkbox-group>
        </el-card>
        <div class="bottom-actions">
          <el-button @click="scrollTo(0)">回到第一题</el-button>
          <el-button type="danger" :loading="submitting" @click="handleSubmit">提交试卷</el-button>
        </div>
      </div>

      <!-- 右侧答题卡 -->
      <div class="answer-card-wrap">
        <AnswerCard
          :questions="examStore.questions"
          :answer-map="examStore.answerMap"
          :current-index="currentIndex"
          @jump="scrollTo"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useExamStore } from '@/store/exam'
import AnswerCard from '@/components/AnswerCard.vue'
import { QUESTION_TYPE_TEXT, DIFFICULTY_TEXT, DIFFICULTY_TAG } from '@/utils/constants'
import { secondsToClock } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()

const starting = ref(false)
const submitting = ref(false)
const submitted = ref(false) // 防止重复交卷（倒计时归零 + 手动点击竞态）
const listRef = ref(null)
const cardRefs = ref([])
const currentIndex = ref(0)

function setCardRef(el, i) {
  cardRefs.value[i] = el
}

/** 选项文本 "A. xxx" → 字母 "A" */
function letter(opt) {
  return opt?.[0] || ''
}

function singleAnswer(qid) {
  const v = examStore.answerMap[qid]
  return Array.isArray(v) ? v[0] || '' : v || ''
}

function multiAnswer(qid) {
  const v = examStore.answerMap[qid]
  return Array.isArray(v) ? [...v] : v ? [v] : []
}

/** 跳转到第 index 题并高亮 */
function scrollTo(index) {
  currentIndex.value = index
  const el = cardRefs.value[index]?.$el
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 滚动时更新当前题号（节流）
let ticking = false
function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    const listEl = listRef.value
    if (listEl) {
      const top = listEl.getBoundingClientRect().top + 140
      let idx = 0
      for (let i = 0; i < cardRefs.value.length; i++) {
        if (cardRefs.value[i]?.$el?.getBoundingClientRect().top <= top) idx = i
      }
      currentIndex.value = idx
    }
    ticking = false
  })
}

/** 时间到 → 自动交卷 */
function handleTimeout() {
  if (submitted.value) return
  ElMessage.warning('考试时间到，系统自动交卷')
  doSubmit(true)
}

/** 手动交卷 → 二次确认 */
async function handleSubmit() {
  if (submitted.value) return
  const unanswered = examStore.totalCount - examStore.answeredCount
  try {
    await ElMessageBox.confirm(
      unanswered > 0
        ? `还有 ${unanswered} 题未作答，确定交卷吗？`
        : '确定提交试卷吗？提交后不可修改。',
      '交卷确认',
      { type: 'warning', confirmButtonText: '确认交卷', cancelButtonText: '再检查一下' }
    )
  } catch {
    return
  }
  doSubmit(false)
}

async function doSubmit(auto) {
  if (submitted.value) return
  submitted.value = true
  submitting.value = true
  try {
    const recordId = examStore.recordId
    await examStore.submit()
    examStore.reset()
    if (auto) {
      ElMessage.info('试卷已自动提交')
    } else {
      ElMessage.success('交卷成功，正在为你判分')
    }
    router.replace({ path: '/record', query: { detail: recordId } })
  } catch (e) {
    submitted.value = false
  } finally {
    submitting.value = false
  }
}

/** 离开页面提醒（非强制） */
function handleBeforeUnload(e) {
  if (examStore.inExam) {
    e.preventDefault()
    e.returnValue = '考试进行中，离开页面将丢失本次考试记录（计时仍会继续），确定离开吗？'
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  // 同一场考试已在内存中（如误触返回后再次进入）：恢复答题，不重复开考
  if (examStore.inExam && examStore.examId === id) {
    examStore.startTimer(handleTimeout)
  } else {
    starting.value = true
    try {
      await examStore.start(id)
      examStore.startTimer(handleTimeout)
    } finally {
      starting.value = false
    }
  }
  listRef.value?.addEventListener('scroll', onScroll, { passive: true })
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  listRef.value?.removeEventListener('scroll', onScroll)
  window.removeEventListener('beforeunload', handleBeforeUnload)
  // 未交卷离开：不销毁考试状态，倒计时继续（返回本页可恢复）
  if (!submitted.value) {
    examStore.startTimer(handleTimeout)
  }
})
</script>

<style scoped>
.exam-do {
  min-height: calc(100vh - 92px);
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  box-shadow: var(--el-box-shadow-light);
}

.exam-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.progress-text {
  font-size: 13px;
  font-weight: 400;
  color: #909399;
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 22px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--el-color-danger);
}

.countdown.danger {
  animation: blink 1s infinite;
}

@keyframes blink {
  50% {
    opacity: 0.4;
  }
}

.exam-body {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.question-list {
  flex: 1;
  min-width: 0;
  max-height: calc(100vh - 160px);
  overflow-y: auto;
  padding-right: 4px;
}

.question-card {
  margin-bottom: 12px;
  scroll-margin-top: 12px;
}

.q-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.q-no {
  font-weight: 700;
  font-size: 16px;
}

.q-stem {
  font-size: 15px;
  margin-bottom: 12px;
}

.option-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.option-item {
  height: auto;
  padding: 6px 0;
  white-space: normal;
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 8px 0 20px;
}

.answer-card-wrap {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 76px;
}

@media (max-width: 900px) {
  .exam-body {
    flex-direction: column-reverse;
  }

  .answer-card-wrap {
    width: 100%;
    position: static;
  }
}
</style>
