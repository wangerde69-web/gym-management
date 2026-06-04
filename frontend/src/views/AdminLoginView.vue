<template>
  <div class="login-wrapper">
    <LoginBrand />
    <div class="login-right">
      <div class="form-wrap">
        <div class="admin-head">
          <n-tag type="warning" size="medium">管理员入口</n-tag>
          <h2>管理员登录</h2>
          <p>请输入管理员账号信息</p>
        </div>
        <n-space vertical size="large" style="width: 100%">
          <n-input v-model:value="f.username" placeholder="管理员账号" size="large" clearable @keyup.enter="handleLogin" />
          <n-input v-model:value="f.password" :type="showPwd ? 'text' : 'password'" placeholder="密码" size="large" @keyup.enter="handleLogin">
            <template #suffix>
              <span style="cursor: pointer; font-size: 18px" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁' }}</span>
            </template>
          </n-input>
          <n-button type="primary" size="large" :loading="loading" block @click="handleLogin">{{ loading ? '登录中' : '登 录' }}</n-button>
        </n-space>
        <div class="back-link"><router-link to="/login">← 返回会员登录</router-link></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { NTag, NInput, NButton, NSpace, useMessage } from 'naive-ui'
import request from '@/api'
import LoginBrand from '@/components/LoginBrand.vue'

// 管理员登录表单状态
const router = useRouter()
const showPwd = ref(false), loading = ref(false)
const f = reactive({ username: '', password: '' })
const message = useMessage()

// 管理员登录处理：验证输入、调用管理端登录接口
function handleLogin() {
  if (!f.username || !f.password) { message.warning('请输入账号和密码'); return }
  loading.value = true
  request.post('/admin/login', f).then(res => {
    loading.value = false
    if (res.code === 200) {
      localStorage.setItem('token', res.token); localStorage.setItem('role', res.role)
      message.success('登录成功'); router.push('/admin')
    } else message.error(res.msg || '登录失败')
  }).catch(() => { loading.value = false; message.error('网络错误') })
}
</script>

<style scoped>
.admin-head { text-align: center; margin-bottom: 28px; }
.admin-head h2 { font-size: var(--text-2xl); font-weight: 800; color: #fff; margin: 14px 0 8px; }
.admin-head p { font-size: var(--text-base); color: var(--text-muted); }
</style>
