<template>
  <div class="login-wrapper">
    <LoginBrand />

    <!-- 右侧：表单 -->
    <div class="login-right">
      <div class="form-wrap">
        <n-tabs v-model:value="tab" size="large" justify-content="center" animated>
          <n-tab-pane name="login" tab="会员登录" />
          <n-tab-pane name="register" tab="立即注册" />
        </n-tabs>

        <!-- 登录 / 注册 两个面板互斥，使用 v-if / v-else-if 避免同时渲染 -->
        <div v-if="tab === 'login'" class="form-area">
          <h2>欢迎回来</h2>
          <p class="form-sub">登录您的会员账户，开始今日训练</p>
          <n-space vertical size="large" style="width: 100%">
            <n-input v-model:value="l.username" placeholder="用户名" size="large" clearable />
            <n-input v-model:value="l.password" :type="showPwd ? 'text' : 'password'" placeholder="密码" size="large" @keyup.enter="handleLogin">
              <template #suffix>
                <span style="cursor: pointer; font-size: 18px" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁' }}</span>
              </template>
            </n-input>
            <n-button type="primary" size="large" :loading="loading" block @click="handleLogin">{{ loading ? '登录中' : '登 录' }}</n-button>
          </n-space>
          <div class="form-ft">还没有账户？<a @click="tab = 'register'">立即注册</a></div>
          <div class="form-admin"><router-link to="/admin-login">管理员登录</router-link></div>
        </div>

        <div v-else-if="tab === 'register'" class="form-area">
          <h2>创建账户</h2>
          <p class="form-sub">加入 GYMCORE，开启健康生活</p>
          <n-space vertical size="large" style="width: 100%" align="center">
            <div class="ava-box" @click="triggerUpload">
              <img v-if="preview" :src="preview" class="ava-img" />
              <span v-else class="ava-empty">点击上传头像</span>
            </div>
            <input ref="inputRef" type="file" accept="image/*" hidden @change="onFileChange" />
            <n-input v-model:value="r.name" placeholder="真实姓名" size="large" clearable style="width: 100%" />
            <n-input v-model:value="r.username" placeholder="设置登录用户名" size="large" clearable style="width: 100%" />
            <n-input v-model:value="r.password" :type="showPwd ? 'text' : 'password'" placeholder="设置登录密码" size="large" style="width: 100%">
              <template #suffix>
                <span style="cursor: pointer; font-size: 18px" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁' }}</span>
              </template>
            </n-input>
            <n-input v-model:value="r.phone" placeholder="手机号码" size="large" clearable style="width: 100%" />
            <n-radio-group v-model:value="r.gender" name="gender" style="width: 100%; display: flex">
              <n-radio-button :value="1" style="flex: 1">男</n-radio-button>
              <n-radio-button :value="0" style="flex: 1">女</n-radio-button>
            </n-radio-group>
            <n-button type="primary" size="large" :loading="loading" block @click="handleReg">{{ loading ? '注册中' : '注 册' }}</n-button>
          </n-space>
          <div class="form-ft">已有账户？<a @click="tab = 'login'">立即登录</a></div>
          <div class="form-admin"><router-link to="/admin-login">管理员登录</router-link></div>
        </div>

        <div class="back-link"><router-link to="/">← 返回首页</router-link></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NTabs, NTabPane, NInput, NButton, NRadioGroup, NRadioButton, NSpace, useMessage } from 'naive-ui'
import request from '@/api'
import LoginBrand from '@/components/LoginBrand.vue'
import { useUpload } from '@/composables/useUpload'

// 页面状态和表单数据
const router = useRouter(), route = useRoute(), tab = ref('login'), showPwd = ref(false), loading = ref(false)
const l = reactive({ username: '', password: '' })
const r = reactive({ name: '', username: '', password: '', phone: '', gender: 1 })
const { inputRef, preview, url: avaUrl, triggerUpload, onFileChange } = useUpload()
const message = useMessage()

// 会员登录处理：验证表单、调用登录接口、存储认证信息
function handleLogin() {
  if (!l.username || !l.password) { message.warning('请输入用户名和密码'); return }
  loading.value = true
  request.post('/member/login', l).then(res => {
    loading.value = false
    if (res.code === 200) {
      localStorage.setItem('token', res.token); localStorage.setItem('username', res.username)
      localStorage.setItem('name', res.name || res.username)
      localStorage.setItem('avatar', res.imageUrl || ''); localStorage.setItem('role', res.role)
      message.success('登录成功'); router.push(route.query.redirect || '/')
    } else message.error(res.msg || '登录失败')
  }).catch(() => { loading.value = false; message.error('网络错误') })
}

// 会员注册处理：验证表单、提交注册请求
function handleReg() {
  if (!r.username || !r.password || !r.name) { message.warning('请填写完整信息'); return }
  loading.value = true
  request.post('/member/register', { ...r, avatar: avaUrl.value }).then(res => {
    loading.value = false
    if (res.code === 200) { message.success('注册成功，请登录'); tab.value = 'login'; l.username = r.username }
    else message.error(res.msg || '注册失败')
  }).catch(() => { loading.value = false; message.error('网络错误') })
}
</script>

<style scoped>
/* ====== 登录页表单样式 ====== */
.form-area { margin-top: 28px; }
.form-area h2 { font-size: var(--text-2xl); font-weight: 800; color: #fff; margin: 0 0 8px; }
.form-sub { font-size: var(--text-base); color: var(--text-muted); margin-bottom: 28px; }
.form-ft { text-align: center; margin-top: 24px; font-size: var(--text-base); color: var(--text-muted); }
.form-ft a { color: var(--gold); cursor: pointer; font-weight: 600; }
.form-admin { text-align: center; margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--border-subtle); }
.form-admin a { font-size: var(--text-sm); color: rgba(255, 255, 255, 0.18); text-decoration: none; }
.form-admin a:hover { color: var(--gold); }
.ava-box { width: 80px; height: 80px; border-radius: 50%; border: 2px dashed rgba(201, 169, 110, 0.3); display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; transition: border-color .2s; }
.ava-box:hover { border-color: var(--gold); }
.ava-img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
.ava-empty { font-size: var(--text-sm); color: var(--text-muted); }
</style>
