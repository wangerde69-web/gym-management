import { ref } from 'vue'
import request from '@/api'
import { useMessage } from 'naive-ui'

// 提取公共的文件上传逻辑：头像/图片上传 + 本地预览
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
