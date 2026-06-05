import { ref } from 'vue'
import request from '@/api'
import { useMessage } from 'naive-ui'

// 完整的文件上传逻辑：inputRef + 本地预览 + 上传 + 返回 URL
export function useUpload() {
  const inputRef = ref(null)
  const preview = ref('')
  const url = ref('')
  const message = useMessage()

  function triggerUpload() {
    inputRef.value?.click()
  }

  function onFileChange(e, onSuccess) {
    const f = e.target.files[0]
    if (!f) return
    // 本地预览
    const rd = new FileReader()
    rd.onload = ev => { preview.value = ev.target.result }
    rd.readAsDataURL(f)
    // 上传到服务器
    const fd = new FormData()
    fd.append('file', f)
    request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => {
      if (res.code === 200) {
        url.value = res.url
        if (onSuccess) onSuccess(res.url)
      } else {
        message.error(res.msg || '上传失败')
      }
    }).catch(() => message.error('上传失败'))
    e.target.value = ''
  }

  return { inputRef, preview, url, triggerUpload, onFileChange }
}

// 轻量版：仅暴露 onFileChange 回调（用于已有自己的 input/file 的场景，避免额外 ref 占用）
export function useFileUploader() {
  const message = useMessage()

  function onFileChange(e, onSuccess) {
    const f = e.target.files[0]
    if (!f) return
    const fd = new FormData()
    fd.append('file', f)
    request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => {
      if (res.code === 200) {
        if (onSuccess) onSuccess(res.url)
      } else {
        message.error(res.msg || '上传失败')
      }
    }).catch(() => message.error('上传失败'))
    e.target.value = ''
  }

  return { onFileChange }
}
