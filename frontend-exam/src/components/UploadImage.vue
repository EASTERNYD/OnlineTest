<template>
  <div class="upload-image">
    <el-upload
      v-if="!modelValue"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="doUpload"
      accept="image/*"
      drag
      class="uploader"
    >
      <el-icon class="uploader-icon"><Plus /></el-icon>
      <div class="el-upload__text">拖拽图片到此处，或<em>点击上传</em></div>
    </el-upload>
    <div v-else class="preview">
      <el-image :src="modelValue" fit="cover" class="preview-img" :preview-src-list="[modelValue]" />
      <div class="preview-actions">
        <el-button size="small" text type="primary" @click="emits('update:modelValue', '')">移除</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'

/**
 * 图片上传组件（轮播图等场景）
 * v-model 绑定图片相对路径（如 /upload/xxx.png），显示时由 Vite 代理转发到后端
 */
const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emits = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function doUpload({ file }) {
  try {
    const data = await uploadFile(file)
    emits('update:modelValue', data.url)
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.uploader {
  width: 220px;
}

.uploader-icon {
  font-size: 40px;
  color: #8c939d;
  margin-bottom: 8px;
}

.preview {
  position: relative;
  width: 220px;
}

.preview-img {
  width: 220px;
  height: 120px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
}

.preview-actions {
  position: absolute;
  bottom: 4px;
  right: 4px;
}
</style>
