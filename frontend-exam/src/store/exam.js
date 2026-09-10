import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { startExam, submitExam } from '@/api/exam'

/**
 * 考试状态（非持久化）
 * - 保存本次考试的试卷信息、题目、答题卡答案与倒计时
 * - 刷新页面会丢失（后端无续考接口，需重新开始）
 */
export const useExamStore = defineStore('exam', () => {
  const examId = ref(null)
  const recordId = ref(null)
  const examName = ref('')
  const totalScore = ref(0)
  const duration = ref(0) // 分钟
  const questions = ref([])
  /** 答题卡：{ [questionId]: 答案字母串 | 字母数组（多选未排序） } */
  const answerMap = reactive({})

  const timeLeft = ref(0) // 剩余秒数
  let timer = null

  const answeredCount = computed(
    () => Object.values(answerMap).filter((v) => (Array.isArray(v) ? v.length > 0 : !!v)).length
  )
  const totalCount = computed(() => questions.value.length)

  /** 开始考试：调后端拿 recordId + 题目（不含答案），并启动倒计时 */
  async function start(examIdArg) {
    const data = await startExam(examIdArg)
    examId.value = data.examId
    recordId.value = data.recordId
    examName.value = data.name
    totalScore.value = data.totalScore
    duration.value = data.duration
    questions.value = data.questions
    timeLeft.value = (data.duration || 0) * 60
    // 清空旧答案
    for (const key of Object.keys(answerMap)) delete answerMap[key]
    startTimer()
    return data
  }

  /** 启动/恢复倒计时，timeLeft 归零时回调 onTimeout（自动交卷） */
  function startTimer(onTimeout) {
    stopTimer()
    timer = setInterval(() => {
      if (timeLeft.value <= 0) {
        stopTimer()
        onTimeout?.()
        return
      }
      timeLeft.value--
    }, 1000)
  }

  function stopTimer() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  /** 交卷：多选答案按字母排序后拼接，键为题目 ID 字符串 */
  async function submit() {
    const answers = {}
    for (const [qid, val] of Object.entries(answerMap)) {
      if (Array.isArray(val)) {
        if (val.length === 0) continue
        answers[qid] = [...val].sort().join('')
      } else if (val) {
        answers[qid] = val
      }
    }
    stopTimer()
    return submitExam(examId.value, { recordId: recordId.value, answers })
  }

  /** 是否正在考试中（用于刷新页面后判断是否残留考试态） */
  const inExam = computed(() => !!recordId.value && timeLeft.value > 0)

  /** 清空考试状态 */
  function reset() {
    stopTimer()
    examId.value = null
    recordId.value = null
    examName.value = ''
    totalScore.value = 0
    duration.value = 0
    questions.value = []
    timeLeft.value = 0
    for (const key of Object.keys(answerMap)) delete answerMap[key]
  }

  return {
    examId,
    recordId,
    examName,
    totalScore,
    duration,
    questions,
    answerMap,
    timeLeft,
    answeredCount,
    totalCount,
    inExam,
    start,
    startTimer,
    stopTimer,
    submit,
    reset
  }
})
