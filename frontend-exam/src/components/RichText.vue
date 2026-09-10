<template>
  <div class="rich-text" :class="{ disabled }">
    <div class="toolbar">
      <el-button-group>
        <el-button size="small" title="加粗" @mousedown.prevent="exec('bold')"><b>B</b></el-button>
        <el-button size="small" title="斜体" @mousedown.prevent="exec('italic')"><i>I</i></el-button>
        <el-button size="small" title="下划线" @mousedown.prevent="exec('underline')"><u>U</u></el-button>
        <el-button size="small" title="清除格式" @mousedown.prevent="exec('removeFormat')">清除格式</el-button>
      </el-button-group>
    </div>
    <div
      ref="editorRef"
      class="editor text-pre-wrap"
      :contenteditable="!disabled"
      @input="onInput"
      @blur="onInput"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

/**
 * 轻量富文本编辑器（公告内容用，基于 contenteditable）
 * v-model 绑定 HTML 字符串；后端 content 为 TEXT 字段，HTML 与纯文本均可存储展示
 */
const props = defineProps({
  modelValue: { type: String, default: '' },
  disabled: { type: Boolean, default: false }
})
const emits = defineEmits(['update:modelValue'])

const editorRef = ref(null)

watch(
  () => props.modelValue,
  (val) => {
    // 外部赋值与当前内容不同步时才回写（避免打断输入）
    if (editorRef.value && editorRef.value.innerHTML !== (val || '')) {
      editorRef.value.innerHTML = val || ''
    }
  },
  { immediate: true }
)

function onInput() {
  emits('update:modelValue', editorRef.value?.innerHTML || '')
}

function exec(cmd) {
  editorRef.value?.focus()
  document.execCommand(cmd, false, null)
  onInput()
}
</script>

<style scoped>
.rich-text {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.rich-text.disabled .editor {
  background: var(--el-fill-color-light);
}

.toolbar {
  padding: 6px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.editor {
  min-height: 180px;
  padding: 10px 12px;
  outline: none;
  overflow-y: auto;
}
</style>
