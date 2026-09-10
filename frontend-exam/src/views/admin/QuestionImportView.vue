<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div class="header">
          <el-button text @click="router.push('/admin/question')">
            <el-icon><Back /></el-icon>返回题目管理
          </el-button>
          <span class="header-title">Excel 批量导入题目</span>
        </div>
      </template>

      <el-steps :active="step" align-center class="steps">
        <el-step title="下载模板" />
        <el-step title="上传并预览" />
        <el-step title="确认导入" />
      </el-steps>

      <!-- 第一步：下载模板 -->
      <div v-if="step === 0" class="step-body">
        <p class="desc">
          请先下载导入模板（.xlsx），按表头填写题目数据：<br />
          <b>题干 / 选项A-D / 答案 / 解析 / 难度 / 分类</b>
        </p>
        <ul class="hint">
          <li>答案：填写 A / B / C / D（仅支持单选题导入）</li>
          <li>难度：1 简单 / 2 中等 / 3 困难</li>
          <li>分类：填分类名，不存在时系统会自动创建</li>
          <li>选项只需填文字，字母前缀（A. / B. …）由系统自动拼接</li>
        </ul>
        <el-button type="primary" :loading="downloading" @click="handleDownload">
          <el-icon><Download /></el-icon>下载模板
        </el-button>
        <el-button @click="step = 1">我已填好，去上传</el-button>
      </div>

      <!-- 第二步：上传预览 -->
      <div v-else-if="step === 1" class="step-body">
        <el-upload
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          class="uploader"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">拖拽 Excel 文件到此处，或<em>点击选择</em></div>
          <template #tip>
            <div class="el-upload__tip">仅支持 .xlsx / .xls 文件，文件不超过 10MB</div>
          </template>
        </el-upload>
        <div class="actions">
          <el-button :disabled="!file" :loading="previewing" type="primary" @click="handlePreview">
            解析预览
          </el-button>
          <el-button :disabled="!file" @click="resetFile">重新选择</el-button>
        </div>

        <el-table v-if="previewList.length" :data="pagedPreview" stripe max-height="400" class="preview-table">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="题干" min-width="220">
            <template #default="{ row }">{{ truncate(row.stem, 34) }}</template>
          </el-table-column>
          <el-table-column label="选项" min-width="220">
            <template #default="{ row }">
              <div class="opts">
                <span v-if="row.optionA">A. {{ row.optionA }}</span>
                <span v-if="row.optionB">B. {{ row.optionB }}</span>
                <span v-if="row.optionC">C. {{ row.optionC }}</span>
                <span v-if="row.optionD">D. {{ row.optionD }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="answer" label="答案" width="70" />
          <el-table-column label="难度" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="DIFFICULTY_TAG[row.difficulty]">{{ DIFFICULTY_TEXT[row.difficulty] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="110" />
        </el-table>
        <el-pagination
          v-if="previewList.length > previewSize"
          class="pagination"
          background
          layout="total, prev, pager, next"
          :total="previewList.length"
          :page-size="previewSize"
          v-model:current-page="previewPage"
        />
        <div v-if="previewList.length" class="import-actions">
          <el-button type="success" :loading="importing" @click="handleImport">
            确认导入（共 {{ previewList.length }} 条）
          </el-button>
        </div>
      </div>

      <!-- 第三步：完成 -->
      <div v-else class="step-body">
        <el-result icon="success" title="导入完成" :sub-title="`成功导入 ${importCount} 道题目`">
          <template #extra>
            <el-button type="primary" @click="router.push('/admin/question')">返回题目管理</el-button>
            <el-button @click="resetAll">继续导入</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { downloadTemplate, previewExcel, importExcel } from '@/api/question'
import { DIFFICULTY_TEXT, DIFFICULTY_TAG } from '@/utils/constants'
import { truncate } from '@/utils/format'

const router = useRouter()
const step = ref(0)
const file = ref(null)
const previewList = ref([])
const previewPage = ref(1)
const previewSize = ref(10)
const downloading = ref(false)
const previewing = ref(false)
const importing = ref(false)
const importCount = ref(0)

const pagedPreview = computed(() => {
  const start = (previewPage.value - 1) * previewSize.value
  return previewList.value.slice(start, start + previewSize.value)
})

function handleFileChange(uploadFile) {
  file.value = uploadFile.raw
  previewList.value = []
}

function handleFileRemove() {
  resetFile()
}

function resetFile() {
  file.value = null
  previewList.value = []
  previewPage.value = 1
}

function resetAll() {
  step.value = 0
  resetFile()
  importCount.value = 0
}

/** 下载模板：blob 转存为文件 */
async function handleDownload() {
  downloading.value = true
  try {
    const blob = await downloadTemplate()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '题目导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (e) {
    // 拦截器已提示
  } finally {
    downloading.value = false
  }
}

async function handlePreview() {
  previewing.value = true
  try {
    previewList.value = await previewExcel(file.value)
    previewPage.value = 1
    ElMessage.success(`解析成功，共 ${previewList.value.length} 条数据，请核对后导入`)
  } catch (e) {
    // 拦截器已提示
  } finally {
    previewing.value = false
  }
}

async function handleImport() {
  importing.value = true
  try {
    importCount.value = await importExcel(file.value)
    step.value = 2
  } catch (e) {
    // 拦截器已提示
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-weight: 600;
}

.steps {
  margin: 16px 0 24px;
}

.step-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.desc {
  text-align: center;
  line-height: 1.8;
  color: #606266;
}

.hint {
  color: #909399;
  font-size: 13px;
  line-height: 1.8;
  padding-left: 20px;
}

.uploader {
  width: 420px;
}

.actions {
  display: flex;
  gap: 12px;
}

.preview-table {
  width: 100%;
}

.opts {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #606266;
  gap: 2px;
}

.pagination {
  margin-top: 8px;
}

.import-actions {
  margin-top: 4px;
}
</style>
