<template>
  <div class="answer-card">
    <div class="legend">
      <span class="legend-item"><i class="dot answered" />已答 {{ answeredCount }}</span>
      <span class="legend-item"><i class="dot unanswered" />未答 {{ totalCount - answeredCount }}</span>
    </div>
    <div class="grid">
      <div
        v-for="(q, i) in questions"
        :key="q.id"
        class="cell"
        :class="cellClass(q.id, i)"
        @click="emits('jump', i)"
      >
        {{ i + 1 }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 答题卡：网格题号，点击跳转，已答/未答/当前题颜色区分
 */
const props = defineProps({
  questions: { type: Array, default: () => [] },
  answerMap: { type: Object, default: () => ({}) },
  currentIndex: { type: Number, default: 0 }
})
const emits = defineEmits(['jump'])

const totalCount = computed(() => props.questions.length)

const answeredCount = computed(
  () =>
    Object.values(props.answerMap).filter((v) =>
      Array.isArray(v) ? v.length > 0 : !!v
    ).length
)

function isAnswered(qid) {
  const v = props.answerMap[qid]
  return Array.isArray(v) ? v.length > 0 : !!v
}

function cellClass(qid, index) {
  return {
    answered: isAnswered(qid),
    current: index === props.currentIndex
  }
}
</script>

<style scoped>
.answer-card {
  background: #fff;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 12px;
}

.legend {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 2px;
  display: inline-block;
}

.dot.answered {
  background: var(--el-color-primary);
}

.dot.unanswered {
  background: #fff;
  border: 1px solid var(--el-border-color);
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.cell {
  width: 36px;
  height: 36px;
  line-height: 34px;
  text-align: center;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  user-select: none;
  transition: all 0.15s;
}

.cell:hover {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.cell.answered {
  background: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
}

.cell.current {
  outline: 2px solid var(--el-color-danger);
  outline-offset: -1px;
}
</style>
