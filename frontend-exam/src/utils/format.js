// 时间与展示格式化工具

/**
 * 后端时间为 `yyyy-MM-dd'T'HH:mm:ss` 字符串，转成 `yyyy-MM-dd HH:mm:ss`
 * @param {string|number|Date} t 后端时间字符串
 */
export function formatTime(t) {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 只取日期部分 yyyy-MM-dd */
export function formatDate(t) {
  return formatTime(t).slice(0, 10)
}

/** 秒数 → `HH:mm:ss`（不足一小时显示 `mm:ss`），用于考试倒计时 */
export function secondsToClock(sec) {
  const s = Math.max(0, Math.floor(sec))
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const ss = s % 60
  const pad = (n) => String(n).padStart(2, '0')
  return h > 0 ? `${pad(h)}:${pad(m)}:${pad(ss)}` : `${pad(m)}:${pad(ss)}`
}

/** 题干超长截断 */
export function truncate(str, len = 40) {
  if (!str) return ''
  return str.length > len ? str.slice(0, len) + '…' : str
}
