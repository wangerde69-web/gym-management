<template>
  <div>
    <!-- 导航栏 -->
    <header class="top-nav">
      <div class="top-nav-inner">
        <router-link to="/" class="brand">GYMCORE</router-link>
        <nav class="nav-links">
          <a v-for="l in navs" :key="l.href" :href="l.href" class="nav-link">{{ l.label }}</a>
          <router-link to="/pose-analysis" class="nav-link">AI 体态分析</router-link>
        </nav>
        <div class="nav-right">
          <UserMenu v-if="isLoggedIn" @command="onUserMenu" />
          <router-link v-else to="/login">
            <n-button type="primary" size="medium">会员登录</n-button>
          </router-link>
        </div>
      </div>
    </header>

    <!-- 主视觉区 -->
    <section class="hero" id="hero">
      <video :src="cfg.promo_video || '/promo.mp4'" autoplay muted loop playsinline class="hero-video" />
      <div class="hero-overlay" />
      <!-- 主视觉区浮动金粒子 -->
      <canvas class="hero-particles" ref="heroParticlesCanvas"></canvas>
      <div class="hero-content">
        <div class="hero-tag" ref="heroTag">{{ cfg.hero_tag || '2026 新赛季' }}</div>
        <h1 class="hero-h1" ref="heroTitle">于此<br/><span class="gold">见证蜕变之美</span></h1>
        <p class="hero-p" ref="heroSub">私属空间 · 精准体系 · 资深领航</p>
        <div class="hero-btns" ref="heroBtns">
          <n-button type="primary" size="large" round @click="scrollTo('#courses')">探索课程</n-button>
          <n-button size="large" round class="btn-ghost" @click="scrollTo('#coaches')">认识教练</n-button>
        </div>
        <div class="hero-stats" ref="heroStats">
          <div class="stat-item"><span class="stat-num">{{ stats.memberCount }}+</span><span class="stat-lbl">注册会员</span></div>
          <div class="stat-item"><span class="stat-num">{{ stats.coachCount }}+</span><span class="stat-lbl">专业教练</span></div>
          <div class="stat-item"><span class="stat-num">{{ stats.courseCount }}+</span><span class="stat-lbl">精品课程</span></div>
        </div>
      </div>
    </section>

    <!-- ====== 场馆展示 — 悬浮光卡阵列 ====== -->
    <section v-if="banners.length" class="venue-section">
      <SectionHeader tag="Features" title="场馆展示" subtitle="品质空间，从这里开始" centered />
      <div class="venue-grid">
        <div
          v-for="(b, i) in banners"
          :key="i"
          class="venue-card"
          :ref="el => venueCards[i] = el"
          :style="{ '--i': i }"
          @mousemove="(e) => onVenueTilt(e, i)"
          @mouseleave="onVenueLeave(i)"
        >
          <div class="venue-card-inner" :ref="el => venueCardInners[i] = el">
            <div class="venue-shine"></div>
            <img :src="b.imageUrl || `/images/gym${(i % 7) + 1}.jpg`" :alt="b.title" class="venue-img" />
            <div class="venue-glow"></div>
            <div class="venue-info">
              <span class="venue-index">0{{ i + 1 }}</span>
              <h3>{{ b.title }}</h3>
              <p>{{ b.subtitle || 'GYMCORE' }}</p>
            </div>
          </div>
          <!-- 连接线 -->
          <div v-if="i < banners.length - 1" class="venue-connector" />
        </div>
      </div>
    </section>

    <!-- ====== 臻选私教课程 — 纵深视差回廊 ====== -->
    <section class="courses-section" id="courses" ref="coursesSection">
      <SectionHeader tag="Exceptional Services" title="臻选私教课程" subtitle="于精密训练中，雕琢理想体态" />
      <div class="courses-grid">
        <div v-if="courses[0]" class="course-featured" @click="$router.push('/course/' + courses[0].id)">
          <div class="course-ft-frame">
            <div class="course-ft-depth">
              <div class="course-ft-bg">
                <img :src="courses[0].imageUrl || '/images/gym7.jpg'" :alt="courses[0].name" />
              </div>
              <div class="course-ft-mid">
                <img :src="courses[0].imageUrl || '/images/gym7.jpg'" :alt="courses[0].name" />
              </div>
            </div>
            <div class="course-ft-overlay" />
            <div class="course-ft-border-pulse">
              <svg viewBox="0 0 100 100" preserveAspectRatio="none">
                <rect x="0.5" y="0.5" width="99" height="99" fill="none" stroke="var(--gold)" stroke-width="0.4" vector-effect="non-scaling-stroke" />
              </svg>
            </div>
            <span class="course-ft-price">¥{{ courses[0].price }}</span>
            <div class="course-ft-info">
              <h3>{{ courses[0].name }}</h3>
              <p>{{ courses[0].description }}</p>
              </div>
          </div>
        </div>
        <div class="course-list" ref="courseList">
          <div
            v-for="(c, i) in courses.slice(1)"
            :key="c.id"
            class="course-row"
            :style="{ '--d': i }"
            @click="$router.push('/course/' + c.id)"
          >
            <div class="course-row-figure">
              <img :src="c.imageUrl || '/images/gym7.jpg'" :alt="c.name" />
              <div class="course-row-shine"></div>
            </div>
            <div class="course-row-info">
              <h3>{{ c.name }}<span class="course-row-underline"></span></h3>
              <p>{{ c.description }}</p>
            </div>
            <div class="course-row-meta">
              <span>{{ c.capacity }}人</span>
              <b>¥{{ c.price }}</b>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ====== 匠心领航者 — 翻牌揭示 + 粒子消散 ====== -->
    <section class="coaches-section" id="coaches">
      <SectionHeader tag="The Master Coaches" title="匠心领航者" subtitle="历经岁月淬炼，专注于人体美学的精准雕琢" centered />
      <div class="coaches-grid" ref="coachesGrid">
        <div
          v-for="(c, i) in coaches"
          :key="c.id"
          class="coach-card"
          :ref="el => coachCards[i] = el"
          @mouseenter="(e) => onCoachEnter(e, c)"
          @mouseleave="(e) => onCoachLeave(e)"
        >
          <div class="coach-card-flipper" :ref="el => coachFlippers[i] = el">
            <!-- 正面 -->
            <div class="coach-front">
              <img :src="c.imageUrl || c.avatar || '/images/gym7.jpg'" :alt="c.name" />
              <div class="coach-front-overlay"></div>
              <div class="coach-front-info">
                <h3>{{ c.name }}</h3>
                <n-tag type="warning" size="medium">{{ c.specialty }}</n-tag>
              </div>
            </div>
            <!-- 背面 -->
            <div class="coach-back">
              <div class="coach-back-pattern"></div>
              <div class="coach-back-content">
                <h3>{{ c.name }}</h3>
                <span class="coach-back-spec">{{ c.specialty }}</span>
                <p>{{ c.intro || '资深教练，拥有丰富的训练经验和专业的教学方法。' }}</p>
                <span class="coach-back-exp">{{ c.experience }}</span>
              </div>
            </div>
          </div>
          <canvas class="coach-particles" :ref="el => coachParticleCanvases[i] = el"></canvas>
        </div>
      </div>
    </section>

    <!-- ====== 器材展示 — 滚动驱动卡片从中心散开 ====== -->
    <section v-if="equipments.length" class="equip-section" id="equipment" ref="equipSection">
      <span class="equip-label">Top Notch Gear</span>
      <div class="equip-stage" ref="equipStage">
        <div
          v-for="(e, i) in equipments"
          :key="e.id"
          class="equip-card"
          :ref="el => equipCards[i] = el"
          :style="getEquipCardStyle(i)"
        >
          <div class="equip-card-inner">
            <div class="equip-card-visual">
              <img :src="e.imageUrl || '/images/gym7.jpg'" :alt="e.name" />
            </div>
            <div class="equip-card-body">
              <span class="equip-card-num">0{{ i + 1 }}</span>
              <h3>{{ e.name }}</h3>
              <div class="equip-card-status">
                <n-tag :type="e.status === 1 ? 'success' : e.status === 2 ? 'warning' : 'error'" size="small">
                  {{ e.status === 1 ? '正常' : e.status === 2 ? '维修中' : '报废' }}
                </n-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="equip-progress">
        <span class="equip-progress-text">{{ equipHint }}</span>
      </div>
    </section>

    <!-- ====== Brand — 保持哑铃变形，增强 ====== -->
    <section class="brand-wrap" id="about">
      <div class="brand-box" ref="brandBox">
        <div class="brand-text-inner" ref="brandText">
          <span class="b-sm">开始你的旅程</span>
          <h2>唤醒身体潜能</h2>
          <span class="b-sub">突破自我极限</span>
        </div>
        <div class="brand-dumbbell" ref="brandDumbbell">
          <svg viewBox="0 0 340 160" fill="none">
            <rect x="0" y="12" width="36" height="136" rx="18" fill="#b8944e"/>
            <rect x="5" y="20" width="26" height="120" rx="13" fill="var(--gold)"/>
            <rect x="8" y="30" width="20" height="100" rx="10" fill="var(--gold-hover)"/>
            <rect x="36" y="65" width="268" height="14" rx="7" fill="var(--gold)"/>
            <rect x="48" y="58" width="244" height="6" rx="3" fill="var(--gold-hover)" opacity="0.5"/>
            <line v-for="n in [70,120,170,220,270]" :key="n" :x1="n" y1="65" :x2="n" y2="79" stroke="rgba(0,0,0,0.15)" stroke-width="1.5"/>
            <rect x="304" y="12" width="36" height="136" rx="18" fill="#b8944e"/>
            <rect x="309" y="20" width="26" height="120" rx="13" fill="var(--gold)"/>
            <rect x="312" y="30" width="20" height="100" rx="10" fill="var(--gold-hover)"/>
          </svg>
        </div>
      </div>
    </section>

    <!-- ====== CTA — 逐字浮现 + 呼吸光晕 + 金粒漂浮 ====== -->
    <section class="cta-wrap" ref="ctaRef">
      <canvas class="cta-particles" ref="ctaParticlesCanvas"></canvas>
      <div class="cta-glow"></div>
      <h2 ref="ctaTitle">不只是汗水，更是蜕变的艺术。</h2>
      <p ref="ctaSub">即刻开启您的专属训练体验。</p>
      <div class="cta-btns" ref="ctaBtns">
        <template v-if="isLoggedIn">
          <n-button type="primary" size="large" round @click="$router.push('/my-cards')">我的会员卡</n-button>
          <n-button size="large" round class="btn-ghost" @click="$router.push('/my-bookings')">我的预约</n-button>
        </template>
        <n-button v-else type="primary" size="large" round @click="$router.push('/login')">立即注册 / 登录</n-button>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <b class="brand">GYMCORE</b>
          <p>专业健身连锁品牌 · 全方位健康管理</p>
        </div>
        <div class="footer-grid">
          <div class="footer-col">
            <h4>关于我们</h4>
            <a @click="showInfo('brand')">品牌故事</a>
            <a href="#coaches">教练团队</a>
            <a @click="showInfo('address')">门店地址</a>
          </div>
          <div class="footer-col">
            <h4>会员中心</h4>
            <router-link to="/login">会员登录</router-link>
            <router-link to="/my-cards">我的会员卡</router-link>
            <router-link to="/my-bookings">我的预约</router-link>
          </div>
        </div>
        <div class="footer-copy">© 2026 GYMCORE. All rights reserved.</div>
      </div>
    </footer>

    <!-- 个人信息对话框 -->
    <n-modal v-model:show="profileVisible" title="修改信息" preset="dialog" positive-text="保存" negative-text="取消" @positive-click="saveProfile" @negative-click="profileVisible = false">
      <div class="profile-form">
        <div class="avatar-upload" @click="avatarInput?.click()">
          <img v-if="pf.avatar" :src="pf.avatar" class="avatar-preview" />
          <span v-else class="upload-placeholder">点击上传</span>
        </div>
        <input ref="avatarInput" type="file" accept="image/*" hidden @change="onAvatarChange" />
        <n-input v-model:value="pf.name" placeholder="姓名" size="large" />
        <n-input v-model:value="pf.username" placeholder="用户名" size="large" />
        <n-input v-model:value="pf.password" type="password" placeholder="留空不修改密码" size="large" />
        <n-input v-model:value="pf.phone" placeholder="手机号" size="large" />
        <n-radio-group v-model:value="pf.gender" name="gender">
          <n-space><n-radio :value="1">男</n-radio><n-radio :value="0">女</n-radio></n-space>
        </n-radio-group>
      </div>
    </n-modal>

    <!-- 信息对话框 -->
    <n-modal v-model:show="infoVisible" :title="infoTitle" preset="dialog" positive-text="关闭" @positive-click="infoVisible = false">
      <div v-html="infoContent" style="line-height: 1.8; font-size: var(--text-base)" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NTag, NModal, NInput, NRadioGroup, NRadio, NSpace, useMessage } from 'naive-ui'
import { animate, stagger } from 'animejs'
import request from '@/api'
import UserMenu from '@/components/UserMenu.vue'
import SectionHeader from '@/components/SectionHeader.vue'
import { useUserMenu } from '@/composables/useUserMenu'
import { useUpload } from '@/composables/useUpload'

const message = useMessage()
const route = useRoute()
const { onMenu: baseOnMenu } = useUserMenu()

// 导航链接、页面数据源和统计数字
const navs = [
  { href: '#hero', label: '首页' }, { href: '#courses', label: '课程' },
  { href: '#equipment', label: '器械' }, { href: '#coaches', label: '教练' },
  { href: '#about', label: '关于' }
]

// 各区块的列表数据和首页统计数据
const banners = ref([]), courses = ref([]), coaches = ref([]), equipments = ref([])
const stats = reactive({ memberCount: 0, coachCount: 0, courseCount: 0 })
const cfg = ref({})
// 登录状态判断（基于本地 Token）
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 引用
const heroTag = ref(null), heroTitle = ref(null)
const heroSub = ref(null), heroBtns = ref(null), heroStats = ref(null)
const heroParticlesCanvas = ref(null)

const venueCards = ref([]), venueCardInners = ref([])

const coursesSection = ref(null), courseList = ref(null)

const coachesGrid = ref(null)
const coachCards = ref([]), coachFlippers = ref([]), coachParticleCanvases = ref([])

const equipSection = ref(null), equipStage = ref(null)
const equipCards = ref([])
const equipSpread = ref(0)
const equipHint = ref('继续向下滑动展开器材')
let equipScrollHandler = null
const brandBox = ref(null), brandText = ref(null), brandDumbbell = ref(null)

const ctaRef = ref(null), ctaParticlesCanvas = ref(null)
const ctaTitle = ref(null), ctaSub = ref(null), ctaBtns = ref(null)

// ─── 动画清理句柄 ───
let heroParticlesAnimId = null, heroResizeHandler = null
let ctaParticlesAnimId = null, ctaResizeHandler = null
let scrollHandler = null
let brandTimer1 = null, brandTimer2 = null
const intersectionObservers = []
const coachParticleTimers = []

// ═══════════════════════════════════════════
// 1. 主视觉区 — 浮动金粒子系统
// ═══════════════════════════════════════════
function initHeroParticles() {
  const canvas = heroParticlesCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const dpr = window.devicePixelRatio || 1
  let w, h

  function resize() {
    const parent = canvas.parentElement
    w = parent.offsetWidth
    h = parent.offsetHeight
    canvas.width = w * dpr
    canvas.height = h * dpr
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }
  resize()
  heroResizeHandler = resize
  window.addEventListener('resize', heroResizeHandler)

  const particles = Array.from({ length: 40 }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 1.5 + 0.5,
    vx: (Math.random() - 0.5) * 0.3,
    vy: (Math.random() - 0.5) * 0.3 - 0.2,
    alpha: Math.random() * 0.5 + 0.1,
    alphaDir: Math.random() > 0.5 ? 1 : -1
  }))

  function draw() {
    ctx.clearRect(0, 0, w, h)
    particles.forEach(p => {
      p.x += p.vx; p.y += p.vy
      if (p.x < 0) p.x = w
      if (p.x > w) p.x = 0
      if (p.y < 0) p.y = h
      if (p.y > h) p.y = 0

      p.alpha += p.alphaDir * 0.005
      if (p.alpha > 0.6) p.alphaDir = -1
      if (p.alpha < 0.1) p.alphaDir = 1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(201,169,110,${p.alpha})`
      ctx.fill()
    })
    heroParticlesAnimId = requestAnimationFrame(draw)
  }
  draw()
}

// ═══════════════════════════════════════════
// 2. 场馆 — 3D 倾斜画廊
// ═══════════════════════════════════════════
function onVenueTilt(e, idx) {
  const card = venueCards.value[idx]
  const inner = venueCardInners.value[idx]
  if (!card || !inner) return
  const rect = card.getBoundingClientRect()
  const x = (e.clientX - rect.left) / rect.width - 0.5
  const y = (e.clientY - rect.top) / rect.height - 0.5
  const px = ((e.clientX - rect.left) / rect.width) * 100
  const py = ((e.clientY - rect.top) / rect.height) * 100
  card.style.setProperty('--mx', px + '%')
  card.style.setProperty('--my', py + '%')
  inner.style.transform = `rotateY(${x * 8}deg) rotateX(${-y * 6}deg) translateZ(4px)`
  inner.style.transition = 'transform 0.15s ease-out'
  card.style.boxShadow = `
    ${-x * 20}px ${-y * 20}px 40px rgba(201,169,110,${0.15 + Math.abs(x) * 0.1}),
    ${x * 10}px ${y * 10}px 20px rgba(0,0,0,0.4)
  `
}

function onVenueLeave(idx) {
  const card = venueCards.value[idx]
  const inner = venueCardInners.value[idx]
  if (!inner) return
  inner.style.transform = 'rotateY(0deg) rotateX(0deg) translateZ(0)'
  inner.style.transition = 'transform 0.6s cubic-bezier(0.23, 1, 0.32, 1)'
  if (card) card.style.boxShadow = ''
}

// ═══════════════════════════════════════════
// 器材 — 滑动展开（滚动驱动）
// ═══════════════════════════════════════════
// 每张卡片的展开目标：以中心为原点向四周扩散
const equipTargets = []

function initEquipTargets() {
  const total = equipCards.value.length
  equipTargets.length = 0
  if (total === 0) return

  const stage = equipStage.value
  const stageW = stage ? stage.clientWidth : 800
  const stageH = stage ? stage.clientHeight : 500
  const cardW = 280
  const cardH = 200
  const gap = 24

  // 可用偏移边界：卡片 edge 不超出 stage
  const maxX = (stageW - cardW) / 2
  const maxY = (stageH - cardH) / 2

  // 按卡片自身尺寸 + 间距排列
  const cols = Math.ceil(Math.sqrt(total))
  const rows = Math.ceil(total / cols)
  const gridW = cols * cardW + (cols - 1) * gap
  const gridH = rows * cardH + (rows - 1) * gap

  for (let i = 0; i < total; i++) {
    const col = i % cols
    const row = Math.floor(i / cols)
    let x = col * (cardW + gap) - (gridW - cardW) / 2
    let y = row * (cardH + gap) - (gridH - cardH) / 2
    // 钳制在 stage 边界内
    x = Math.max(-maxX, Math.min(maxX, x))
    y = Math.max(-maxY, Math.min(maxY, y))
    equipTargets.push({ x, y, r: 0 })
  }
}

function getEquipCardStyle(idx) {
  const s = equipSpread.value
  const t = equipTargets[idx]
  if (!t) return {}

  // 堆叠：全部叠在中心，小幅错开
  const stackX = (idx - (equipCards.value.length - 1) / 2) * 4
  const stackY = idx * 2
  const stackR = 0

  // 展开：向各自方向散开
  const x = stackX * (1 - s) + t.x * s
  const y = stackY * (1 - s) + t.y * s
  const r = stackR * (1 - s) + t.r * s
  const opacity = 0.3 + s * 0.7
  const zIndex = Math.round(s * 10) + idx + 1

  return {
    transform: `translateX(${x}px) translateY(${y}px) rotate(${r}deg)`,
    opacity,
    zIndex
  }
}

function onEquipScroll() {
  const section = equipSection.value
  if (!section) return
  const viewH = window.innerHeight
  const stage = equipStage.value
  if (!stage) return
  const stageRect = stage.getBoundingClientRect()
  const stageCenter = stageRect.top + stageRect.height / 2
  const viewCenter = viewH / 2
  const totalDist = stageRect.height / 2 + viewH / 2
  const scrolled = viewH - stageCenter
  let progress = scrolled / totalDist
  progress = Math.max(0, Math.min(1, progress))

  equipSpread.value = progress

  if (progress < 0.1) equipHint.value = '继续向下滑动展开器材'
  else if (progress < 0.5) equipHint.value = '正在展开...'
  else if (progress < 0.9) equipHint.value = '即将完全展开'
  else equipHint.value = '已全部展开'
}

// 窗口大小变化时重新计算目标位置
function onEqResize() {
  initEquipTargets()
}

function initEquipScroll() {
  initEquipTargets()
  equipScrollHandler = onEquipScroll
  window.addEventListener('scroll', equipScrollHandler, { passive: true })
  window.addEventListener('resize', onEqResize, { passive: true })
  // 初始计算一次
  setTimeout(onEquipScroll, 200)
}

// ═══════════════════════════════════════════
// 3. COURSES — 纵深视差 + 行项目入场
// ═══════════════════════════════════════════
function initCoursesParallax() {
  // 特色课程的深度层视差滚动
  const bgLayer = document.querySelector('.course-ft-bg')
  const midLayer = document.querySelector('.course-ft-mid')
  if (!bgLayer || !midLayer) return

  let ticking = false
  function onScroll() {
    if (ticking) return
    ticking = true
    requestAnimationFrame(() => {
      const sectionRect = coursesSection.value?.getBoundingClientRect()
      if (!sectionRect || sectionRect.bottom < 0 || sectionRect.top > window.innerHeight) { ticking = false; return }
      const progress = 1 - (sectionRect.top / window.innerHeight)
      const clamped = Math.max(0, Math.min(1, progress))
      // 背景层移动更慢（深度视差）
      bgLayer.style.transform = `translateY(${clamped * 15}px)`
      // 中间层轻微移动
      midLayer.style.transform = `translateY(${clamped * 6}px)`
      ticking = false
    })
  }
  window.addEventListener('scroll', onScroll, { passive: true })
  scrollHandler = onScroll

  // 课程行项目依次入场 — 添加类名触发 CSS 动画
  const obs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.querySelectorAll('.course-row').forEach((row, i) => {
          row.classList.add('course-row-in')
        })
        obs.disconnect()
      }
    })
  }, { threshold: 0.1 })
  if (courseList.value) obs.observe(courseList.value)
  intersectionObservers.push(obs)
}

// ═══════════════════════════════════════════
// 4. 教练 — 翻牌揭示 + 粒子消散
// ═══════════════════════════════════════════
const activeCoachIdx = ref(-1)

function onCoachEnter(e, coach) {
  const idx = coachCards.value.findIndex(c => {
    try { return c && c.contains && c.contains(e.target) } catch { return false }
  })
  if (idx === -1) return
  activeCoachIdx.value = idx

  const flipper = coachFlippers.value[idx]
  if (flipper) flipper.classList.add('flipped')

  // 悬停时生成粒子效果
  spawnCoachParticles(idx, e)
}

function onCoachLeave(e) {
  const prev = activeCoachIdx.value
  if (prev !== -1) {
    const flipper = coachFlippers.value[prev]
    if (flipper) flipper.classList.remove('flipped')
    activeCoachIdx.value = -1
  }
}

function spawnCoachParticles(idx, e) {
  const canvas = coachParticleCanvases.value[idx]
  const card = coachCards.value[idx]
  if (!canvas || !card) return

  const rect = card.getBoundingClientRect()
  const dpr = window.devicePixelRatio || 1
  canvas.width = rect.width * dpr
  canvas.height = rect.height * dpr
  canvas.style.width = rect.width + 'px'
  canvas.style.height = rect.height + 'px'
  canvas.style.position = 'absolute'
  canvas.style.inset = '0'
  canvas.style.pointerEvents = 'none'
  canvas.style.zIndex = '10'

  const ctx = canvas.getContext('2d')
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)

  const mx = e.clientX - rect.left
  const my = e.clientY - rect.top
  const particles = Array.from({ length: 20 }, () => ({
    x: mx,
    y: my,
    r: Math.random() * 2 + 0.5,
    vx: (Math.random() - 0.5) * 6,
    vy: (Math.random() - 0.5) * 6 - 2,
    life: 1,
    decay: 0.015 + Math.random() * 0.025
  }))

  let animId
  function draw() {
    ctx.clearRect(0, 0, rect.width, rect.height)
    let alive = false
    particles.forEach(p => {
      p.x += p.vx
      p.y += p.vy
      p.vy += 0.05
      p.life -= p.decay
      if (p.life <= 0) return
      alive = true
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(201,169,110,${p.life * 0.8})`
      ctx.fill()
    })
    if (alive) {
      animId = requestAnimationFrame(draw)
    } else {
      ctx.clearRect(0, 0, rect.width, rect.height)
    }
  }
  draw()
  coachParticleTimers.push(setTimeout(() => cancelAnimationFrame(animId), 2000))
}

function initCoachesScroll() {
  const obs = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && coachCards.value.length) {
        coachCards.value.forEach((card, i) => {
          if (!card) return
          card.style.opacity = '0'
          card.style.transform = 'scale(0.9) translateY(40px)'
          card.style.transition = `all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1) ${i * 0.12}s`
          requestAnimationFrame(() => {
            card.style.opacity = '1'
            card.style.transform = 'scale(1) translateY(0)'
          })
        })
        obs.disconnect()
      }
    })
  }, { threshold: 0.1 })
  if (coachesGrid.value) obs.observe(coachesGrid.value)
  intersectionObservers.push(obs)
}

// ═══════════════════════════════════════════
// 5. 行动号召 — 逐字浮现 + 呼吸光晕 + 浮动金色
// ═══════════════════════════════════════════
function initCTAParticles() {
  const canvas = ctaParticlesCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const dpr = window.devicePixelRatio || 1
  let w, h

  function resize() {
    const parent = canvas.parentElement
    w = parent.offsetWidth
    h = parent.offsetHeight
    canvas.width = w * dpr
    canvas.height = h * dpr
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  }
  resize()
  ctaResizeHandler = resize
  window.addEventListener('resize', ctaResizeHandler)

  const particles = Array.from({ length: 25 }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 1.8 + 0.3,
    vx: (Math.random() - 0.5) * 0.25,
    vy: -Math.random() * 0.5 - 0.1,
    alpha: Math.random() * 0.4 + 0.05,
    wobble: Math.random() * Math.PI * 2
  }))

  function draw() {
    ctx.clearRect(0, 0, w, h)
    particles.forEach(p => {
      p.wobble += 0.01
      p.x += p.vx + Math.sin(p.wobble) * 0.15
      p.y += p.vy
      if (p.y < -10) { p.y = h + 10; p.x = Math.random() * w }
      if (p.x < -10) p.x = w + 10
      if (p.x > w + 10) p.x = -10

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(201,169,110,${p.alpha})`
      ctx.fill()
    })
    ctaParticlesAnimId = requestAnimationFrame(draw)
  }
  draw()
}

function initCTAReveal() {
  const obs = new IntersectionObserver(([entry]) => {
    if (entry.isIntersecting) {
      // 标题逐字动画
      const titleEl = ctaTitle.value
      if (titleEl) {
        const text = titleEl.textContent
        titleEl.innerHTML = ''
        const chars = text.split('').map(ch => {
          const span = document.createElement('span')
          span.textContent = ch
          span.style.display = 'inline-block'
          span.style.opacity = '0'
          span.style.transform = 'translateY(30px) rotateX(-90deg)'
          titleEl.appendChild(span)
          return span
        })

        animate(chars, {
          translateY: [30, 0],
          rotateX: [-90, 0],
          opacity: [0, 1],
          delay: stagger(50),
          duration: 800,
          ease: 'outExpo'
        })
      }

      // 副标题上滑淡入
      if (ctaSub.value) {
        animate(ctaSub.value, {
          translateY: [20, 0],
          opacity: [0, 1],
          delay: 1200,
          duration: 700,
          ease: 'outCubic'
        })
      }

      // 按钮
      if (ctaBtns.value) {
        const btnEls = Array.from(ctaBtns.value.children)
        animate(btnEls, {
          translateY: [16, 0],
          opacity: [0, 1],
          delay: stagger(120, { start: 1600 }),
          duration: 600,
          ease: 'outCubic'
        })
      }

      obs.disconnect()
    }
  }, { threshold: 0.3 })
  if (ctaRef.value) obs.observe(ctaRef.value)
  intersectionObservers.push(obs)
}

// ═══════════════════════════════════════════
// 共享
// ═══════════════════════════════════════════
function initHero() {
  const heroTargets = [heroTag.value, heroTitle.value, heroSub.value, heroBtns.value].filter(Boolean)
  if (heroTargets.length) {
    animate(heroTargets, {
      translateY: [30, 0],
      opacity: [0, 1],
      delay: stagger(120),
      duration: 800,
      ease: 'outCubic'
    })
  }

  if (heroStats.value) {
    const statEls = Array.from(heroStats.value.querySelectorAll('.stat-item'))
    if (statEls.length) {
      animate(statEls, {
        translateY: [20, 0],
        opacity: [0, 1],
        delay: stagger(100, { start: 600 }),
        duration: 600,
        ease: 'outCubic'
      })
    }
  }
}

function initBrandMorph() {
  if (brandBox.value) {
    const obs = new IntersectionObserver(([e]) => {
      if (e.isIntersecting) {
        brandBox.value.classList.add('brand-morph')
        brandTimer1 = setTimeout(() => { if (brandDumbbell.value) brandDumbbell.value.style.opacity = '0' }, 3400)
        brandTimer2 = setTimeout(() => { if (brandText.value) brandText.value.style.opacity = '1' }, 3800)
        obs.disconnect()
      }
    }, { threshold: 0.25 })
    obs.observe(brandBox.value)
    intersectionObservers.push(obs)
  }
}

// ═══════════════════════════════════════════
// 用户 / 个人信息 / 信息展示
// ═══════════════════════════════════════════
function onUserMenu(key) {
  if (key === 'profile') openProfile()
  else baseOnMenu(key)
}

const profileVisible = ref(false)
const pf = reactive({ id: null, avatar: '', name: '', username: '', password: '', phone: '', gender: 1 })
const avatarInput = ref(null)

const { onFileChange } = useUpload()

function onAvatarChange(e) {
  onFileChange(e, url => { pf.avatar = url })
}

function openProfile() {
  request.get('/member/info').then(r => {
    if (r.code === 200 && r.data) Object.assign(pf, { ...r.data, password: '' })
    profileVisible.value = true
  })
}

function saveProfile() {
  request.put('/member/update', pf).then(r => {
    if (r.code === 200) {
      message.success('保存成功')
      profileVisible.value = false
      localStorage.setItem('avatar', pf.avatar)
      localStorage.setItem('name', pf.name)
    }
  })
}

const infoVisible = ref(false), infoTitle = ref(''), infoContent = ref('')

function showInfo(type) {
  if (type === 'brand') {
    infoTitle.value = '品牌故事'
    infoContent.value = (cfg.value.brand_story || '').replace(/\n/g, '<br>') || 'GYMCORE 高端健身品牌。'
  } else {
    infoTitle.value = '门店地址'
    try {
      const l = JSON.parse(cfg.value.store_address || '[]')
      infoContent.value = Array.isArray(l) && l.length
        ? l.map(a => {
            let h = ''; if (a.image) h += '<img src="' + a.image + '" style="max-width:100%;margin:8px 0;border-radius:8px;display:block"/>'
            h += '<p>' + (a.address || '') + '</p><p>电话: ' + (a.phone || '') + '</p>'; return h
          }).join('<hr style="border-color:#333;margin:12px 0">')
        : '暂无门店信息'
    } catch { infoContent.value = cfg.value.store_address || '暂无门店信息' }
  }
  infoVisible.value = true
}

/* 平滑滚动到指定锚点区域 */
function scrollTo(s) { document.querySelector(s)?.scrollIntoView({ behavior: 'smooth' }) }

// 加载首页全部数据（轮播图、课程、教练、器材、配置、统计）
function fetchIndex() {
  request.get('/front/index').then(r => {
    if (r.code === 200) {
      banners.value = r.banners || []
      courses.value = r.courses || []
      coaches.value = (r.coaches || []).map(c => ({ ...c, imageUrl: c.avatar }))
      equipments.value = r.equipments || []
      cfg.value = r.config || {}
      stats.memberCount = r.memberCount || 0
      stats.coachCount = r.coachCount ?? ((r.coaches || []).length || 0)
      stats.courseCount = r.courseCount ?? ((r.courses || []).length || 0)
    }
  })
}

// 从其他页面跳转时自动打开修改资料弹窗
watch(() => route.query.profile, (v) => { if (v === '1') openProfile() })

// 页面挂载：拉取数据并延迟初始化各类动画效果
onMounted(() => {
  fetchIndex()
  setTimeout(() => {
    initHero()
    initHeroParticles()
    initEquipScroll()
    initCoursesParallax()
    initCoachesScroll()
    initCTAParticles()
    initCTAReveal()
    initBrandMorph()
  }, 300)
})

// 页面卸载：清理动画帧、事件监听器、定时器和观察器
onUnmounted(() => {
  if (heroParticlesAnimId) cancelAnimationFrame(heroParticlesAnimId)
  if (heroResizeHandler) window.removeEventListener('resize', heroResizeHandler)
  if (ctaParticlesAnimId) cancelAnimationFrame(ctaParticlesAnimId)
  if (ctaResizeHandler) window.removeEventListener('resize', ctaResizeHandler)
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  if (equipScrollHandler) window.removeEventListener('scroll', equipScrollHandler)
  window.removeEventListener('resize', onEqResize)
  if (brandTimer1) clearTimeout(brandTimer1)
  if (brandTimer2) clearTimeout(brandTimer2)
  intersectionObservers.forEach(o => o.disconnect())
  coachParticleTimers.forEach(t => clearTimeout(t))
})
</script>

<style scoped>
/* ====== 主视觉区 ====== */
.hero { position: relative; height: 100vh; display: flex; align-items: center; overflow: hidden; }
.hero-video { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
.hero-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.7); }
.hero-particles { position: absolute; inset: 0; z-index: 1; pointer-events: none; }
.hero-content { position: relative; z-index: 2; width: 100%; padding: 0 var(--content-padding-x); }
.hero-tag { display: inline-block; padding: 8px 20px; border: 1px solid rgba(201, 169, 110, 0.4); border-radius: 24px; font-size: var(--text-xs); letter-spacing: 3px; color: var(--gold); margin-bottom: 28px; }
.hero-h1 { font-size: var(--text-hero); font-weight: 900; line-height: 0.92; letter-spacing: -2px; color: #fff; margin-bottom: 24px; }
.gold { background: linear-gradient(135deg, var(--gold), var(--gold-hover)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero-p { font-size: var(--text-lg); letter-spacing: 3px; color: rgba(255, 255, 255, 0.55); margin-bottom: 28px; font-weight: 300; }
.hero-btns { display: flex; gap: 18px; margin-bottom: 80px; flex-wrap: wrap; }
.btn-ghost { color: rgba(255, 255, 255, 0.7); border: 1px solid rgba(255, 255, 255, 0.2); background: transparent; }
.hero-stats { display: flex; align-items: center; gap: clamp(32px, 5vw, 72px); }
.stat-item { text-align: center; }
.stat-num { font-size: 40px; font-weight: 800; color: #fff; line-height: 1; display: block; }
.stat-lbl { font-size: var(--text-sm); letter-spacing: 2px; color: rgba(255, 255, 255, 0.35); margin-top: 6px; display: block; }

/* ═══════════════════════════════════════════
   VENUE — 悬浮光卡阵列
   ═══════════════════════════════════════════ */
.venue-section { padding: var(--section-gap) 0; }
.venue-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 28px; padding: 0 var(--content-padding-x); perspective: 1400px; }
.venue-card { border-radius: 18px; overflow: visible; position: relative; height: 380px; cursor: pointer; transform-style: preserve-3d; animation: venueFloatIn 1.2s cubic-bezier(0.25, 0.46, 0.45, 0.94) both; animation-delay: calc(var(--i) * 0.18s); }
@keyframes venueFloatIn {
  from { opacity: 0; transform: translateY(80px); }
  to { opacity: 1; transform: translateY(0); }
}
.venue-card-inner { width: 100%; height: 100%; position: relative; transform-style: preserve-3d; will-change: transform; border-radius: 18px; overflow: hidden; background: var(--bg-card); }
.venue-shine { position: absolute; inset: 0; z-index: 2; pointer-events: none; background: radial-gradient(circle at var(--mx, 50%) var(--my, 50%), rgba(201,169,110,0.2) 0%, transparent 60%); opacity: 0; transition: opacity 0.4s; }
.venue-card:hover .venue-shine { opacity: 1; }
.venue-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.7s cubic-bezier(0.16, 1, 0.3, 1); }
.venue-card:hover .venue-img { transform: scale(1.08); }
.venue-glow { position: absolute; inset: -2px; z-index: 1; pointer-events: none; border-radius: 20px; border: 1px solid rgba(201,169,110,0); transition: all 0.5s; }
.venue-card:hover .venue-glow { border-color: rgba(201,169,110,0.25); box-shadow: 0 0 40px rgba(201,169,110,0.08), inset 0 0 60px rgba(201,169,110,0.03); }
.venue-info { position: absolute; bottom: 0; left: 0; right: 0; padding: 32px 28px; z-index: 3; background: linear-gradient(transparent, rgba(0, 0, 0, 0.92)); transform: translateY(0); transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.venue-card:hover .venue-info { transform: translateY(-4px); }
.venue-index { font-size: 11px; letter-spacing: 4px; color: var(--gold); font-weight: 700; display: block; margin-bottom: 8px; }
.venue-info h3 { font-size: var(--text-xl); font-weight: 800; color: #fff; margin-bottom: 4px; }
.venue-info p { font-size: var(--text-sm); color: rgba(255, 255, 255, 0.35); }
.venue-connector { position: absolute; right: -18px; top: 50%; width: 10px; height: 1px; background: linear-gradient(90deg, rgba(201,169,110,0.3), transparent); z-index: 0; transform: translateY(-50%); }
.venue-card:last-child .venue-connector { display: none; }

/* ═══════════════════════════════════════════
   COURSES — 纵深视差回廊
   ═══════════════════════════════════════════ */
.courses-section { padding: var(--section-gap) 0; overflow: hidden; }
.courses-grid { display: grid; grid-template-columns: 1.4fr 1fr; gap: 0; padding: 0 var(--content-padding-x); align-items: start; }
.course-featured { cursor: pointer; will-change: transform; }
.course-ft-frame { position: relative; height: 560px; border-radius: 18px; overflow: hidden; }
.course-ft-depth { position: absolute; inset: 0; }
.course-ft-bg { position: absolute; inset: -40px; will-change: transform; }
.course-ft-bg img { width: 100%; height: 100%; object-fit: cover; filter: blur(8px) brightness(0.5); transform: scale(1.1); }
.course-ft-mid { position: absolute; inset: 0; will-change: transform; }
.course-ft-mid img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.8s cubic-bezier(0.16, 1, 0.3, 1); }
.course-featured:hover .course-ft-mid img { transform: scale(1.04); }
.course-ft-overlay { position: absolute; inset: 0; background: linear-gradient(transparent 20%, rgba(0, 0, 0, 0.88)); z-index: 1; }
.course-ft-border-pulse { position: absolute; inset: 0; z-index: 3; pointer-events: none; opacity: 0; transition: opacity 0.6s; }
.course-featured:hover .course-ft-border-pulse { opacity: 1; }
.course-ft-border-pulse svg { width: 100%; height: 100%; }
.course-ft-border-pulse rect { animation: borderPulse 2s ease-in-out infinite; }
@keyframes borderPulse {
  0%, 100% { stroke-opacity: 0.3; }
  50% { stroke-opacity: 1; }
}
.course-ft-price { position: absolute; top: 24px; right: 24px; background: var(--gold); color: #000; padding: 10px 24px; border-radius: 24px; font-size: var(--text-lg); font-weight: 700; z-index: 2; }
.course-ft-info { position: absolute; bottom: 0; left: 0; right: 0; padding: 36px; z-index: 2; }
.course-ft-info h3 { font-size: var(--text-2xl); font-weight: 800; color: #fff; margin-bottom: 10px; }
.course-ft-info p { font-size: var(--text-sm); color: rgba(255, 255, 255, 0.35); margin-bottom: 22px; }

.course-list { will-change: transform; }
.course-row { display: flex; align-items: center; gap: 22px; padding: 20px 28px; border-bottom: 1px solid var(--border-subtle); cursor: pointer; transition: all 0.3s; will-change: transform, opacity; opacity: 0; transform: translateX(40px); }
.course-row.course-row-in { animation: courseRowIn 1s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards; }
@keyframes courseRowIn {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}
.course-row:nth-child(1).course-row-in { animation-delay: 0s; }
.course-row:nth-child(2).course-row-in { animation-delay: 0.15s; }
.course-row:nth-child(3).course-row-in { animation-delay: 0.30s; }
.course-row:nth-child(4).course-row-in { animation-delay: 0.45s; }
.course-row:nth-child(5).course-row-in { animation-delay: 0.60s; }
.course-row:nth-child(6).course-row-in { animation-delay: 0.75s; }
.course-row:nth-child(7).course-row-in { animation-delay: 0.90s; }
.course-row:hover { background: rgba(201, 169, 110, 0.04); transform: translateX(4px); }
.course-row-figure { width: 80px; height: 80px; border-radius: var(--radius-md); overflow: hidden; flex-shrink: 0; position: relative; }
.course-row-figure img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.course-row:hover .course-row-figure img { transform: scale(1.08); }
.course-row-shine { position: absolute; inset: 0; pointer-events: none; background: linear-gradient(120deg, transparent 30%, rgba(201,169,110,0.2) 50%, transparent 70%); transform: translateX(-100%); transition: transform 0.6s; }
.course-row:hover .course-row-shine { transform: translateX(100%); }
.course-row-info { flex: 1; min-width: 0; }
.course-row-info h3 { font-size: var(--text-lg); font-weight: 700; color: #fff; margin-bottom: 6px; display: inline-block; position: relative; }
.course-row-underline { position: absolute; bottom: -2px; left: 0; width: 0; height: 1.5px; background: var(--gold); transition: width 0.4s cubic-bezier(0.16, 1, 0.3, 1); }
.course-row:hover .course-row-underline { width: 100%; }
.course-row-info p { font-size: var(--text-sm); color: var(--text-secondary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.course-row-meta { text-align: right; flex-shrink: 0; }
.course-row-meta span { font-size: var(--text-sm); color: var(--text-secondary); display: block; margin-bottom: 6px; }
.course-row-meta b { font-size: 24px; font-weight: 800; color: var(--gold); }

/* ═══════════════════════════════════════════
   教练 — 翻牌 + 粒子
   ═══════════════════════════════════════════ */
.coaches-section { padding: var(--section-gap) 0; }
.coaches-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 28px; padding: 0 var(--content-padding-x); }
.coach-card { position: relative; border-radius: var(--radius-lg); overflow: visible; cursor: pointer; height: 420px; perspective: 1000px; }
.coach-card-flipper { width: 100%; height: 100%; position: relative; transform-style: preserve-3d; transition: transform 0.7s cubic-bezier(0.4, 0, 0.2, 1); border-radius: var(--radius-lg); }
.coach-card-flipper.flipped { transform: rotateY(180deg); }

/* 正面 */
.coach-front { position: absolute; inset: 0; backface-visibility: hidden; border-radius: var(--radius-lg); overflow: hidden; }
.coach-front img { width: 100%; height: 100%; object-fit: cover; object-position: top; }
.coach-front-overlay { position: absolute; inset: 0; background: linear-gradient(transparent 50%, rgba(0, 0, 0, 0.85)); }
.coach-front-info { position: absolute; bottom: 0; left: 0; right: 0; padding: 24px; display: flex; flex-wrap: wrap; align-items: center; gap: 10px; }
.coach-front-info h3 { font-size: var(--text-xl); font-weight: 800; color: #fff; width: 100%; }

/* 背面 */
.coach-back { position: absolute; inset: 0; backface-visibility: hidden; border-radius: var(--radius-lg); overflow: hidden; background: #1a1510; transform: rotateY(180deg); display: flex; align-items: center; justify-content: center; }
.coach-back-pattern { position: absolute; inset: 0; background:
  radial-gradient(circle at 20% 30%, rgba(201,169,110,0.08) 0%, transparent 50%),
  radial-gradient(circle at 80% 70%, rgba(201,169,110,0.05) 0%, transparent 50%);
}
.coach-back-content { position: relative; z-index: 1; padding: 32px; text-align: center; }
.coach-back-content h3 { font-size: var(--text-xl); font-weight: 800; color: var(--gold); margin-bottom: 8px; }
.coach-back-spec { font-size: var(--text-sm); color: var(--text-muted); letter-spacing: 2px; display: block; margin-bottom: 18px; }
.coach-back-content p { font-size: var(--text-sm); color: var(--text-secondary); line-height: 1.7; margin-bottom: 20px; }
.coach-back-exp { font-size: var(--text-xs); color: var(--gold); letter-spacing: 1px; border: 1px solid rgba(201,169,110,0.3); padding: 6px 16px; border-radius: 16px; }

.coach-particles { position: absolute; inset: 0; pointer-events: none; z-index: 10; }

/* ═══════════════════════════════════════════
   EQUIPMENT — 滑动展开
   ═══════════════════════════════════════════ */
.equip-section { padding: var(--section-gap) 0; position: relative; display: flex; flex-direction: column; justify-content: flex-start; }
.equip-label { display: block; text-align: center; font-size: var(--text-xs); letter-spacing: 4px; color: var(--gold); font-weight: 600; text-transform: uppercase; margin-bottom: 40px; padding-top: 40px; }
.equip-stage { position: relative; height: 500px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.equip-stage::after { content: ''; position: absolute; top: 50%; left: 50%; width: 12px; height: 12px; border-radius: 50%; background: var(--gold); transform: translate(-50%, -50%); box-shadow: 0 0 20px rgba(201,169,110,0.4); z-index: 0; opacity: 0.6; }
.equip-card { position: absolute; left: 50%; top: 50%; width: 280px; height: 200px; margin-left: -140px; margin-top: -100px; border-radius: 16px; overflow: hidden; cursor: default; border: 1px solid var(--border-subtle); background: rgba(16,16,22,0.95); backdrop-filter: blur(4px); will-change: transform, opacity; transition: none; }
.equip-card-inner { width: 100%; height: 100%; }
.equip-card-visual { width: 100%; height: 100%; }
.equip-card-visual img { width: 100%; height: 100%; object-fit: cover; }
.equip-card-body { position: absolute; bottom: 0; left: 0; right: 0; padding: 18px 20px; background: linear-gradient(transparent, rgba(0,0,0,0.92)); }
.equip-card-num { font-size: 10px; letter-spacing: 3px; color: var(--gold); font-weight: 700; display: block; margin-bottom: 4px; }
.equip-card-body h3 { font-size: 16px; font-weight: 800; color: #fff; margin-bottom: 6px; }
.equip-card-status { display: flex; }
.equip-progress { text-align: center; margin-top: 24px; }
.equip-progress-text { font-size: 13px; color: var(--text-muted); letter-spacing: 1px; }

/* ====== 品牌变形（不变） ====== */
.brand-wrap { padding: 120px 0 160px; display: flex; justify-content: center; background: #1a1510; }
.brand-box { position: relative; overflow: hidden; text-align: center; max-width: 580px; width: 90%; padding: 56px var(--content-padding-x); border-radius: 44px; background: linear-gradient(145deg, var(--gold), #a8874e); }
.brand-morph .brand-text-inner { opacity: 0; pointer-events: none; transition: opacity 0.4s; }
.brand-dumbbell { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; opacity: 0; pointer-events: none; transition: opacity 0.6s ease; }
.brand-morph .brand-dumbbell { opacity: 1; }
.brand-dumbbell svg { width: 280px; height: 160px; transform: rotateZ(-90deg); transition: transform 1.2s cubic-bezier(0.34, 1.56, 0.64, 1); }
.brand-morph .brand-dumbbell svg { transform: rotateZ(0deg); }
.b-sm { font-size: var(--text-xs); letter-spacing: 3px; color: rgba(0, 0, 0, 0.4); font-weight: 600; text-transform: uppercase; display: block; margin-bottom: 24px; }
.brand-box h2 { font-size: clamp(34px, 5vw, 52px); font-weight: 900; color: #000; line-height: 1.05; margin-bottom: 16px; }
.b-sub { font-size: var(--text-lg); color: rgba(255, 255, 255, 0.55); font-weight: 300; font-style: italic; letter-spacing: 1px; }

/* ═══════════════════════════════════════════
   行动号召 — 逐字浮现 + 呼吸光晕 + 粒子
   ═══════════════════════════════════════════ */
.cta-wrap { padding: 120px var(--content-padding-x); text-align: center; position: relative; overflow: hidden; }
.cta-particles { position: absolute; inset: 0; pointer-events: none; z-index: 0; }
.cta-glow { position: absolute; top: 50%; left: 50%; width: 500px; height: 500px; transform: translate(-50%, -50%); border-radius: 50%; background: radial-gradient(circle, rgba(201,169,110,0.12) 0%, transparent 70%); z-index: 0; pointer-events: none; animation: ctaBreathe 4s ease-in-out infinite; }
@keyframes ctaBreathe {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.4); opacity: 1; }
}
.cta-wrap h2 { font-size: clamp(28px, 4vw, 44px); font-weight: 800; color: #fff; margin-bottom: 18px; letter-spacing: -1px; position: relative; z-index: 1; }
.cta-wrap p { font-size: var(--text-base); color: var(--text-muted); margin-bottom: 40px; font-weight: 300; position: relative; z-index: 1; }
.cta-btns { display: flex; gap: 18px; justify-content: center; flex-wrap: wrap; position: relative; z-index: 1; }

/* ====== 页脚 ====== */
.footer { padding: 64px 0 0; background: var(--bg-card); }
.footer-inner { padding: 0 var(--content-padding-x); }
.footer-brand { margin-bottom: 40px; }
.footer-brand p { font-size: var(--text-sm); color: var(--text-muted); margin-top: 8px; }
.footer-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; padding: 36px 0; border-top: 1px solid var(--border-subtle); }
.footer-col h4 { font-size: var(--text-sm); font-weight: 700; letter-spacing: 2px; color: rgba(255, 255, 255, 0.55); margin-bottom: 14px; }
.footer-col a { display: block; font-size: var(--text-base); color: var(--text-muted); text-decoration: none; cursor: pointer; padding: 4px 0; transition: color .2s; }
.footer-col a:hover { color: var(--gold); }
.footer-copy { padding: 24px 0; text-align: center; font-size: var(--text-xs); color: rgba(255, 255, 255, 0.1); }

/* ====== 个人信息 ====== */
.profile-form { display: flex; flex-direction: column; align-items: center; gap: 18px; padding: 10px 0; }
.avatar-upload { width: 80px; height: 80px; border-radius: 50%; border: 2px dashed rgba(201, 169, 110, 0.3); display: flex; align-items: center; justify-content: center; cursor: pointer; overflow: hidden; transition: border-color .2s; }
.avatar-upload:hover { border-color: var(--gold); }
.avatar-preview { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { font-size: var(--text-sm); color: var(--text-muted); }

</style>