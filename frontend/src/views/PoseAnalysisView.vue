<template>
  <div class="page-dark">
    <!-- 导航栏 -->
    <header class="top-nav">
      <div class="top-nav-inner">
        <router-link to="/" class="brand">GYMCORE</router-link>
        <nav class="nav-links">
          <router-link to="/" class="nav-link">首页</router-link>
          <span class="nav-link active">AI 体态分析</span>
        </nav>
      </div>
    </header>

    <!-- 标题 -->
    <section class="pose-title">
      <h1>AI 体态分析</h1>
      <p>上传训练照片 · 智能骨骼分析 · 关节角度检测</p>
    </section>

    <section class="pose-main">
      <!-- 上传 -->
      <div v-if="!loading && !res" class="upload-zone" @click="fi?.click()" @dragover.prevent @drop.prevent="onDrop">
        <input ref="fi" type="file" accept="image/*" hidden @change="onFile" />
        <div class="upload-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c9a96e" stroke-width="1.5"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M17 8l-5-5-5 5M12 3v12"/></svg>
        </div>
        <p class="upload-text">点击或拖拽上传训练照片</p>
        <p class="upload-hint">支持 JPG / PNG，建议全身照、侧面视角</p>
      </div>

      <!-- 提示 -->
      <div v-if="!loading && !res" class="tips-grid">
        <div v-for="(t, i) in tips" :key="i" class="tip-card">
          <span class="tip-num">{{ i + 1 }}</span>
          <div>
            <b>{{ t.title }}</b>
            <p>{{ t.desc }}</p>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner" />
        <p>正在分析姿态...</p>
        <span>AI 检测骨骼关键点，计算关节角度</span>
      </div>

      <!-- 结果 -->
      <div v-if="res && !loading" class="result-grid">
        <div class="result-image">
          <div ref="cw" class="canvas-wrap">
            <div style="position: relative">
              <img ref="ri" :src="iu" @load="draw" crossorigin="anonymous" class="result-img" />
              <canvas ref="oc" class="result-canvas" />
            </div>
          </div>
        </div>
        <div class="result-panel">
          <h3>关节角度</h3>
          <div class="angles-grid">
            <div v-for="(v, k) in res.angles" :key="k" class="angle-item">
              <span>{{ angleLabel(k) }}</span>
              <b :style="{ color: angleColor(k, v) }">{{ v }}°</b>
            </div>
          </div>
          <h3>改善建议</h3>
          <div v-for="(s, i) in res.suggestions" :key="i" class="suggestion-item">
            <span class="sug-num">{{ i + 1 }}</span>
            <span>{{ s }}</span>
          </div>
          <n-button type="primary" size="large" block @click="reset">重新分析</n-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from 'vue'
import request from '@/api'
import { NButton, useMessage } from 'naive-ui'

// 体态分析页面状态：文件输入、加载态、分析结果、图片URL
const fi = ref(null), loading = ref(false), res = ref(null), iu = ref('')
// 结果展示相关 DOM 引用
const ri = ref(null), oc = ref(null), cw = ref(null)
const message = useMessage()

// 上传拍照提示
const tips = [
  { title: '选择姿势', desc: '深蹲、硬拉、弓步蹲等全身动作效果最佳' },
  { title: '拍摄角度', desc: '侧面拍摄，确保全身可见，手臂不遮挡躯干' },
  { title: '查看结果', desc: 'AI 自动标注骨骼关键点，计算关节角度并给出建议' }
]

// 文件选择和拖拽上传入口
function onFile(e) { const f = e.target.files?.[0]; if (f) upload(f) }
function onDrop(e) { const f = e.dataTransfer.files?.[0]; if (f) upload(f) }

// 上传图片并调用 AI 体态分析接口
async function upload(file) {
  loading.value = true; res.value = null
  if (iu.value) URL.revokeObjectURL(iu.value)
  iu.value = URL.createObjectURL(file)
  const fd = new FormData(); fd.append('file', file)
  try {
    const r = await request.post('/pose/analyze', fd, { headers: { 'Content-Type': 'multipart/form-data' }, timeout: 120000 })
    if (r.code === 200) { res.value = r.data; await nextTick() }
    else { message.error(r.msg || '分析失败'); iu.value = '' }
  } catch { message.error('分析请求失败'); iu.value = '' }
  finally { loading.value = false }
}

// 在图片上绘制骨骼关键点和连线（姿态分析结果可视化）
function draw() {
  const img = ri.value, canvas = oc.value; if (!img || !canvas || !res.value) return
  const w = cw.value.clientWidth, s = w / img.naturalWidth, h = img.naturalHeight * s
  canvas.width = w; canvas.height = h; canvas.style.width = w + 'px'; canvas.style.height = h + 'px'
  const ctx = canvas.getContext('2d'), kps = res.value.keypoints, bones = res.value.bones

  bones.forEach(([a, b]) => {
    const p1 = kps[a], p2 = kps[b]; if (!p1 || !p2 || p1.visibility < 0.3 || p2.visibility < 0.3) return
    ctx.beginPath(); ctx.moveTo(p1.x * s, p1.y * s); ctx.lineTo(p2.x * s, p2.y * s)
    ctx.strokeStyle = 'rgba(201,169,110,0.8)'; ctx.lineWidth = 3; ctx.stroke()
  })

  kps.forEach((kp, i) => {
    if (kp.visibility < 0.3) return
    ctx.beginPath(); ctx.arc(kp.x * s, kp.y * s, 5, 0, 2 * Math.PI)
    ctx.fillStyle = [5, 6, 11, 12, 13, 14, 15, 16].includes(i) ? '#f5f0e8' : 'rgba(245,240,232,0.5)'
    ctx.fill(); ctx.strokeStyle = '#c9a96e'; ctx.lineWidth = 2; ctx.stroke()
  });

  [[13, res.value.angles.left_knee], [14, res.value.angles.right_knee]].forEach(([idx, angle]) => {
    const kp = kps[idx]; if (!kp || kp.visibility < 0.3) return
    const x = kp.x * s, y = kp.y * s
    ctx.font = 'bold 16px sans-serif'; ctx.fillStyle = '#f5f0e8'; ctx.strokeStyle = '#0d0d0d'; ctx.lineWidth = 3
    const text = angle + '°', m = ctx.measureText(text)
    ctx.strokeText(text, x - m.width / 2, y - 16); ctx.fillText(text, x - m.width / 2, y - 16)
  })
}

// 关节角度字段名中文映射
function angleLabel(k) {
  return { left_knee: '左膝', right_knee: '右膝', left_hip: '左髋', right_hip: '右髋', left_ankle: '左踝', right_ankle: '右踝', trunk_lean_left: '躯干左', trunk_lean_right: '躯干右' }[k] || k
}

// 根据角度值返回对应颜色（绿色正常、黄色警告、红色异常）
function angleColor(k, v) {
  if (k.includes('knee')) { if (v < 70) return '#f87171'; if (v < 85 || v > 130) return '#fbbf24'; return '#4ade80' }
  if (k.includes('trunk')) { if (v < 25 || v > 55) return '#fbbf24'; return '#4ade80' }
  return 'rgba(245,240,232,0.8)'
}

// 重置页面状态，允许重新上传分析
function reset() {
  res.value = null
  if (iu.value) { URL.revokeObjectURL(iu.value); iu.value = '' }
}

// 页面卸载时释放 ObjectURL
onBeforeUnmount(() => { if (iu.value) URL.revokeObjectURL(iu.value) })
</script>

<style scoped>
/* ====== 标题 ====== */
.pose-title { text-align: center; padding-top: calc(var(--nav-height) + 40px); padding-bottom: 28px; }
.pose-title h1 { font-size: var(--text-2xl); font-weight: 800; color: var(--gold); margin-bottom: 10px; }
.pose-title p { font-size: var(--text-base); color: var(--text-muted); }

/* ====== 主体 ====== */
.pose-main { max-width: 1100px; margin: 0 auto; padding: 0 var(--content-padding-x) 80px; }

/* 上传 */
.upload-zone { border: 2px dashed var(--border-subtle); border-radius: var(--radius-lg); padding: 56px 5vw; text-align: center; cursor: pointer; transition: all 0.3s; background: rgba(255, 255, 255, 0.005); }
.upload-zone:hover { border-color: rgba(201, 169, 110, 0.3); background: rgba(201, 169, 110, 0.02); }
.upload-icon { margin-bottom: 16px; }
.upload-text { font-size: var(--text-lg); font-weight: 600; color: rgba(255, 255, 255, 0.65); margin-bottom: 8px; }
.upload-hint { font-size: var(--text-base); color: var(--text-muted); }

/* 提示 */
.tips-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-top: 24px; }
.tip-card { display: flex; gap: 12px; padding: 20px; border-radius: var(--radius-md); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.005); }
.tip-num { width: 32px; height: 32px; border-radius: 50%; background: var(--gold); color: #000; display: flex; align-items: center; justify-content: center; font-size: var(--text-sm); font-weight: 700; flex-shrink: 0; }
.tip-card b { font-size: var(--text-sm); color: rgba(255, 255, 255, 0.65); display: block; margin-bottom: 4px; }
.tip-card p { font-size: var(--text-sm); color: var(--text-secondary); line-height: 1.5; margin: 0; }

/* 加载中 */
.loading-state { text-align: center; padding: 80px 0; }
.spinner { width: 40px; height: 40px; border: 2px solid rgba(201, 169, 110, 0.15); border-top-color: var(--gold); border-radius: 50%; margin: 0 auto 20px; animation: spin 0.8s linear infinite; }
.loading-state p { font-size: var(--text-lg); font-weight: 600; color: rgba(255, 255, 255, 0.6); margin-bottom: 8px; }
.loading-state span { font-size: var(--text-base); color: var(--text-muted); }

/* 结果 */
.result-grid { display: grid; grid-template-columns: 1fr 360px; gap: 24px; }
.result-image { padding: 16px; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.005); }
.canvas-wrap { overflow: hidden; border-radius: var(--radius-sm); }
.result-img { width: 100%; display: block; border-radius: var(--radius-sm); }
.result-canvas { position: absolute; top: 0; left: 0; pointer-events: none; }

.result-panel { padding: 24px; border-radius: var(--radius-lg); border: 1px solid var(--border-subtle); background: rgba(255, 255, 255, 0.005); display: flex; flex-direction: column; gap: 16px; }
.result-panel h3 { font-size: var(--text-base); font-weight: 700; color: var(--gold); border-bottom: 1px solid rgba(201, 169, 110, 0.12); padding-bottom: 10px; margin: 0; }

.angles-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.angle-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; border-radius: var(--radius-sm); background: rgba(255, 255, 255, 0.01); border: 1px solid transparent; transition: border-color .2s; cursor: default; }
.angle-item:hover { border-color: rgba(201, 169, 110, 0.15); }
.angle-item span { font-size: var(--text-sm); color: var(--text-secondary); }
.angle-item b { font-size: var(--text-lg); }

.suggestion-item { display: flex; gap: 10px; font-size: var(--text-sm); padding: 10px; border-radius: var(--radius-sm); background: rgba(201, 169, 110, 0.02); }
.sug-num { width: 20px; height: 20px; border-radius: 50%; background: var(--gold); color: #000; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.suggestion-item span:last-child { color: rgba(255, 255, 255, 0.5); line-height: 1.5; }

@keyframes spin { to { transform: rotate(360deg); } }
</style>