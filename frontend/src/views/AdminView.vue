<template>
  <div class="admin-page">
    <n-layout has-sider class="admin-layout">
      <!-- 侧边栏 -->
      <n-layout-sider
        bordered
        :width="260"
        :collapsed-width="64"
        :collapsed="sideCollapsed"
        collapse-mode="width"
        show-trigger
        @collapse="sideCollapsed = true"
        @expand="sideCollapsed = false"
        content-style="display:flex;flex-direction:column;height:100%;background:#0e0e0e"
      >
        <div class="aside-brand">管理后台</div>
        <n-config-provider :theme="darkTheme" :theme-overrides="sidebarTheme">
          <n-menu :options="menuOptions" :value="active" @update:value="onMenuSelect" class="menu" />
        </n-config-provider>
        <div class="aside-footer">
          <div class="aside-user">
            <span class="aside-avatar">{{ admin.nickname?.[0] || 'A' }}</span>
            <div>
              <div class="aside-name">{{ admin.nickname || '管理员' }}</div>
              <div class="aside-role">系统管理员</div>
            </div>
          </div>
          <n-button size="medium" text style="color: rgba(255,255,255,0.3)" @click="logout">退出登录</n-button>
        </div>
      </n-layout-sider>

      <!-- 主内容 -->
      <n-layout-content>
        <div class="main-header">
          <h2>{{ title }}</h2>
          <span class="main-clock">{{ now }}</span>
        </div>
        <div class="main-body">
          <!-- 仪表盘 -->
          <template v-if="active === 'dashboard'">
            <div class="stat-row" style="display:flex;gap:16px;margin-bottom:20px">
              <div v-for="s in stats" :key="s.title" style="flex:1">
                <n-card size="small">
                  <div class="stat-card">
                    <span class="stat-icon">{{ s.icon }}</span>
                    <div>
                      <div class="stat-val">{{ s.value }}</div>
                      <div class="stat-label">{{ s.title }}</div>
                    </div>
                  </div>
                </n-card>
              </div>
            </div>
            <n-grid :cols="2" :x-gap="16" responsive="screen" :item-responsive="true">
              <n-grid-item span="2 m:1">
                <n-card title="课程预约统计" size="small"><div ref="bChart" style="height: 300px" /></n-card>
              </n-grid-item>
              <n-grid-item span="2 m:1">
                <n-card title="收入趋势" size="small"><div ref="iChart" style="height: 300px" /></n-card>
              </n-grid-item>
            </n-grid>
          </template>

          <!-- 表格视图 -->
          <n-card v-if="isTab" size="small">
            <template #header>
              <div class="table-toolbar">
                <span class="table-title">{{ title }}</span>
                <div class="table-actions">
                  <n-input v-if="searchable" v-model:value="search" placeholder="搜索..." clearable size="medium" style="width: 220px" />
                  <n-button v-if="creatable" type="primary" size="medium" @click="openEdit(null)">添加</n-button>
                  <n-button v-if="exportable" size="medium" @click="doExport">导出Excel</n-button>
                </div>
              </div>
            </template>
            <div style="overflow-x: auto">
              <n-data-table :columns="cols" :data="filtered" :single-line="false" size="large" :scroll-x="900" />
            </div>
          </n-card>

          <!-- 设置 -->
          <template v-if="active === 'settings'">
            <n-card title="门店基本设置" size="small" style="margin-bottom: 16px">
              <n-space vertical size="large" style="max-width: 600px">
                <n-input v-model:value="st.hero_tag" placeholder="顶部标签，如: 2026 新赛季" size="large" />
                <div>
                  <label class="field-label">首页视频</label>
                  <div class="s-video" @click="suRefs.promo_video?.click()">
                    <video v-if="st.promo_video" :src="st.promo_video" class="s-video-inner" muted loop />
                    <span v-else class="s-img-empty">点击上传</span>
                  </div>
                  <input :ref="el => { if (el) suRefs.promo_video = el }" type="file" accept="video/*" hidden @change="e => suUpload(e, 'promo_video')" />
                </div>
                <div>
                  <label class="field-label">收款码</label>
                  <div class="s-img" @click="suRefs.pay_qr?.click()">
                    <img v-if="st.pay_qr" :src="st.pay_qr" class="s-img-inner" />
                    <span v-else class="s-img-empty">点击上传</span>
                  </div>
                  <input :ref="el => { if (el) suRefs.pay_qr = el }" type="file" accept="image/*" hidden @change="e => suUpload(e, 'pay_qr')" />
                </div>
                <n-input v-model:value="st.brand_story" type="textarea" :rows="4" placeholder="品牌故事" size="large" />
                <n-button type="primary" size="large" @click="saveSettings">保存设置</n-button>
              </n-space>
            </n-card>

            <n-card size="small">
              <template #header>
                <div class="table-toolbar">
                  <span class="table-title">门店地址管理</span>
                  <n-button type="primary" size="medium" @click="addAddr">添加地址</n-button>
                </div>
              </template>
              <n-data-table :columns="addrCols" :data="addrs" size="large" />
            </n-card>
          </template>
        </div>
      </n-layout-content>
    </n-layout>

    <!-- 编辑对话框 -->
    <n-modal v-model:show="editVis" :title="editTitle" preset="dialog" positive-text="保存" negative-text="取消" style="width: 560px; max-width: 90vw" @positive-click="doSave" @negative-click="editVis = false">
      <n-space vertical size="large" align="center" style="width: 100%">
        <div v-if="editDef.img" class="s-img" @click="upRef?.click()">
          <img v-if="ef[editDef.img]" :src="ef[editDef.img]" class="s-img-inner" />
          <span v-else class="s-img-empty">点击上传</span>
        </div>
        <input ref="upRef" type="file" accept="image/*" hidden @change="onUp" />
        <template v-for="f in editDef.fields" :key="f.key">
          <n-input v-if="f.t === 'input'" v-model:value="ef[f.key]" :placeholder="f.l" size="large" style="width: 100%" />
          <n-input-number v-else-if="f.t === 'num'" v-model:value="ef[f.key]" :min="f.min || 0" :step="f.prec ? 0.01 : 1" size="large" style="width: 100%" />
          <n-input v-else-if="f.t === 'area'" v-model:value="ef[f.key]" type="textarea" :rows="f.rows || 3" :placeholder="f.l" size="large" style="width: 100%" />
          <n-select v-else-if="f.t === 'sel'" v-model:value="ef[f.key]" :placeholder="f.l" :options="f.opts.map(o => ({ label: o.l, value: o.v }))" size="large" style="width: 100%" :consistent-menu-width="false" />
        </template>
      </n-space>
    </n-modal>

    <!-- 地址对话框 -->
    <n-modal v-model:show="addrVis" title="编辑门店地址" preset="dialog" positive-text="保存" negative-text="取消" style="width: 480px; max-width: 90vw" @positive-click="doSaveAddr" @negative-click="addrVis = false">
      <n-space vertical size="large" align="center" style="width: 100%">
        <div class="s-img" @click="auRef?.click()">
          <img v-if="af.image" :src="af.image" class="s-img-inner" />
          <span v-else class="s-img-empty">点击上传</span>
        </div>
        <input ref="auRef" type="file" accept="image/*" hidden @change="onAu" />
        <n-input v-model:value="af.address" placeholder="详细地址" size="large" style="width: 100%" />
        <n-input v-model:value="af.phone" placeholder="联系电话" size="large" style="width: 100%" />
      </n-space>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch, h } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { NLayout, NLayoutSider, NLayoutContent, NMenu, NButton, NConfigProvider, NGrid, NGridItem, NCard, NDataTable, NInput, NInputNumber, NSelect, NModal, NSpace, NTag, useMessage, useDialog, darkTheme } from 'naive-ui'
import request from '@/api'
import { useFileUploader } from '@/composables/useUpload'

// 侧边栏菜单主题覆写（暗色背景 + 金色高亮）
const sidebarTheme = {
  Menu: {
    itemTextColor: '#ffffff',
    itemTextColorHover: '#ffffff',
    itemTextColorActive: '#e8d49e',
    itemColorActive: 'rgba(201, 169, 110, 0.2)',
    itemColorActiveHover: 'rgba(201, 169, 110, 0.2)',
    itemColorHover: 'rgba(255, 255, 255, 0.08)',
    borderColorActive: '#e8d49e'
  }
}

// 页面状态：当前选中菜单项、搜索关键词、实时时钟、管理员信息
const router = useRouter(), active = ref('dashboard'), search = ref('')
const now = ref(''), admin = reactive({ nickname: '' })
const sideCollapsed = ref(false)
let nowTimer = null
const message = useMessage()
const dialog = useDialog()

// 侧边栏菜单选项定义
const menuOptions = [
  { key: 'dashboard', label: '数据看板' },
  { key: 'card-approval', label: '购卡审批' },
  { key: 'booking', label: '预约管理' },
  { key: 'member', label: '会员管理' },
  { key: 'course', label: '课程管理' },
  { key: 'coach', label: '教练管理' },
  { key: 'card', label: '会员卡管理' },
  { key: 'equipment', label: '器材管理' },
  { key: 'banner', label: '场馆展示' },
  { key: 'settings', label: '门店信息' }
]

// 菜单选中切换、标题联动
function onMenuSelect(key) { active.value = key }

const title = computed(() => menuOptions.find(m => m.key === active.value)?.label || '')

// 统计
const stats = ref([
  { title: '办卡人数', value: 0, icon: '👥' }, { title: '课程数量', value: 0, icon: '🏋️' },
  { title: '教练数量', value: 0, icon: '👨‍🏫' }, { title: '预约总数', value: 0, icon: '📅' },
  { title: '器材数量', value: 0, icon: '🔧' }
])

// 数据
const members = ref([]), courses = ref([]), coaches = ref([]), cardTypes = ref([])
const equipments = ref([]), banners = ref([]), bookings = ref([]), cards = ref([])

// 通用数据拉取函数和各模块数据加载器
function dFetch(api, target) { request.get(api).then(r => { if (r.code === 200) target.value = r.data || [] }).catch(() => message.error('数据加载失败')) }
const fMembers = () => dFetch('/member/all', members)
const fCourses = () => dFetch('/course/all', courses)
const fCoaches = () => dFetch('/coach/all', coaches)
const fCardTypes = () => dFetch('/cardtype/all', cardTypes)
const fEquipments = () => dFetch('/equipment/all', equipments)
const fBanners = () => dFetch('/banner/all', banners)
const fBookings = () => dFetch('/booking/all', bookings)
const fCards = () => dFetch('/card/list', cards)
// 仪表盘数据加载
const fDashboard = () => request.get('/stat/dashboard').then(r => {
  if (r.code === 200) {
    const d = r.data
    stats.value = [
      { title: '办卡人数', value: d.cardMemberCount || 0, icon: '👥' },
      { title: '课程数量', value: d.courseCount || 0, icon: '🏋️' },
      { title: '教练数量', value: d.coachCount || 0, icon: '👨‍🏫' },
      { title: '预约总数', value: d.bookingCount || 0, icon: '📅' },
      { title: '器材数量', value: d.equipmentCount || 0, icon: '🔧' }
    ]
  }
}).catch(() => message.error('加载仪表盘数据失败'))

// 根据当前菜单项计算：是否显示表格、是否支持搜索/新增/导出
const isTab = computed(() => !['dashboard', 'settings'].includes(active.value))
const searchable = computed(() => ['member', 'course', 'coach', 'card', 'equipment'].includes(active.value))
const creatable = computed(() => ['course', 'coach', 'card', 'equipment', 'banner'].includes(active.value))
const exportable = computed(() => ['card-approval', 'booking', 'member'].includes(active.value))

// 根据当前菜单项映射对应的数据源
const tableData = computed(() => {
  const m = { booking: bookings.value, 'card-approval': cards.value, member: members.value, course: courses.value, coach: coaches.value, card: cardTypes.value, equipment: equipments.value, banner: banners.value }
  return m[active.value] || []
})

// 表格搜索过滤：按名称、标题、用户名等字段模糊匹配
const filtered = computed(() => {
  if (!search.value) return tableData.value
  const q = search.value.toLowerCase()
  return tableData.value.filter(r => ['name', 'cardName', 'title', 'username', 'phone', 'specialty'].some(k => (r[k] || '').toLowerCase().includes(q)))
})

// 列辅助函数
// 状态值到标签文字和颜色的映射表
// 注意：不同实体的 status 取值集合不同，命名上做了区分（cardStatus / coachStatus / courseStatus 等）
const tagMap = {
  // 通用 status：用于会员，0=停用, 1=正常
  status: { 0: ['停用', 'warning'], 1: ['正常', 'success'] },
  bookingStatus: { 0: ['待确认', 'warning'], 1: ['已确认', 'success'], 2: ['已取消', 'info'] },
  cardStatus: { 0: ['待审核', 'warning'], 1: ['生效中', 'success'], 2: ['已过期', 'info'], 3: ['已退款', 'error'], 4: ['未支付', 'info'], 5: ['未支付待审', 'warning'] },
  coachStatus: { 1: ['在职', 'success'], 0: ['离职', 'error'] },
  courseStatus: { 1: ['上架', 'success'], 0: ['下架', 'info'] },
  equipmentStatus: { 1: ['正常', 'success'], 2: ['维修中', 'warning'], 0: ['报废', 'error'] },
  bannerStatus: { 1: ['启用', 'success'], 0: ['禁用', 'info'] }
}

// 格式化时间：去掉 ISO 格式中的 T，如 "2024-06-03T10:30:00" → "2024-06-03 10:30:00"
function fmtTime(val) { return val ? val.replace('T', ' ') : '' }

// 渲染状态标签和操作按钮的辅助函数
function renderTag(row, key, map) {
  const [label, type] = map[row[key]] || ['未知', 'info']
  return h(NTag, { type, size: 'medium' }, () => label)
}

function renderActions(row, actions) {
  const btns = actions(row).filter(a => a.if !== false)
  return h('div', { style: 'display:flex;gap:6px;flex-wrap:nowrap' }, btns.map(a =>
    h(NButton, { size: 'small', type: a.key === 'del' || a.key === 'no' ? 'error' : 'primary', secondary: true, onClick: a.fn }, () => a.label)
  ))
}

// 根据教练ID查找教练姓名
function coachName(row) {
  const c = coaches.value.find(x => x.id === row.coachId)
  return c ? c.name : '未分配'
}

// 列定义
const cols = computed(() => {
  const A = active.value
  if (A === 'card-approval') return [
    { title: '卡ID', key: 'id' }, { title: '会员', key: 'memberName', render: row => row.memberName || ('ID:' + row.memberId) },
    { title: '卡种', key: 'cardTypeKey', render: row => ({ WEEKLY: '周卡', MONTHLY: '月卡', YEARLY: '年卡', COUNT: '次卡' }[row.cardTypeKey] || row.cardTypeKey) },
    { title: '价格', key: 'price', render: row => `¥${row.price}` },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.cardStatus) },
    { title: '创建时间', key: 'createTime', render: row => fmtTime(row.createTime) },
    { title: '过期时间', key: 'endDate' },
    { title: '操作', key: 'actions', width: 180, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '通过', fn: () => confirmAction('确认通过？', () => request.put('/card/approve/' + r.id), fCards), if: r.status === 0 }, { key: 'no', label: '拒绝', fn: () => confirmAction('确认拒绝？', () => request.delete('/card/' + r.id), fCards), if: r.status === 0 }, { key: 'del', label: '删除', fn: () => del('card', r.id) }]) }
  ]
  if (A === 'booking') return [
    { title: 'ID', key: 'id' }, { title: '会员', key: 'memberName' }, { title: '课程', key: 'courseName' },
    { title: '时间段', key: 'bookingTime' },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.bookingStatus) },
    { title: '创建时间', key: 'createTime', render: row => fmtTime(row.createTime) },
    { title: '操作', key: 'actions', width: 180, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '通过', fn: () => confirmAction('确认通过？', () => request.put('/booking/approve/' + r.id), fBookings), if: r.status === 0 }, { key: 'no', label: '拒绝', fn: () => confirmAction('确认拒绝？', () => request.delete('/booking/' + r.id), fBookings), if: r.status === 0 }, { key: 'del', label: '删除', fn: () => del('booking', r.id) }]) }
  ]
  if (A === 'member') return [
    { title: 'ID', key: 'id' }, { title: '头像', key: 'avatar', width: 60, render: row => h('img', { src: row.avatar, style: 'width:36px;height:36px;border-radius:50%;object-fit:cover' }) },
    { title: '姓名', key: 'name' }, { title: '用户名', key: 'username' },
    { title: '手机号', key: 'phone' }, { title: '性别', key: 'gender', render: row => row.gender === 1 ? '男' : '女' },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.status) },
    { title: '注册时间', key: 'createTime', render: row => fmtTime(row.createTime) },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('member', r.id) }]) }
  ]
  if (A === 'course') return [
    { title: 'ID', key: 'id' }, { title: '封面', key: 'imageUrl', width: 70, render: row => h('img', { src: row.imageUrl, style: 'width:56px;height:36px;border-radius:4px;object-fit:cover' }) },
    { title: '名称', key: 'name' }, { title: '容量', key: 'capacity', render: row => `${row.capacity}人` },
    { title: '价格', key: 'price', render: row => `¥${row.price}` },
    { title: '教练', key: 'coachId', render: row => coachName(row) },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.courseStatus) },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('course', r.id) }]) }
  ]
  if (A === 'coach') return [
    { title: 'ID', key: 'id' }, { title: '头像', key: 'avatar', width: 60, render: row => h('img', { src: row.avatar, style: 'width:36px;height:36px;border-radius:50%;object-fit:cover' }) },
    { title: '姓名', key: 'name' }, { title: '专长', key: 'specialty' },
    { title: '经验', key: 'experience' }, { title: '电话', key: 'phone' },
    { title: '简介', key: 'intro', ellipsis: { tooltip: true } },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.coachStatus) },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('coach', r.id) }]) }
  ]
  if (A === 'card') return [
    { title: 'ID', key: 'id' }, { title: '封面', key: 'coverImage', width: 70, render: row => h('img', { src: row.coverImage, style: 'width:56px;height:36px;border-radius:4px;object-fit:cover' }) },
    { title: '名称', key: 'cardName' }, { title: '标识', key: 'cardKey' },
    { title: '价格', key: 'price', render: row => `¥${row.price}` }, { title: '有效期', key: 'validityDays', render: row => { const u = { DAY: '天', MONTH: '个月', YEAR: '年' }; return `${row.validityDays}${u[row.validityUnit] || '天'}` } }, { title: '排序', key: 'sortOrder' },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.courseStatus) },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('cardtype', r.id) }]) }
  ]
  if (A === 'equipment') return [
    { title: 'ID', key: 'id' }, { title: '图片', key: 'imageUrl', width: 70, render: row => h('img', { src: row.imageUrl, style: 'width:56px;height:36px;border-radius:4px;object-fit:cover' }) },
    { title: '名称', key: 'name' }, { title: '位置', key: 'location' },
    { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.equipmentStatus) },
    { title: '备注', key: 'remark', ellipsis: { tooltip: true } },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('equipment', r.id) }]) }
  ]
  if (A === 'banner') return [
    { title: 'ID', key: 'id' }, { title: '预览', key: 'imageUrl', width: 100, render: row => h('img', { src: row.imageUrl, style: 'width:80px;height:48px;border-radius:4px;object-fit:cover' }) },
    { title: '标题', key: 'title' }, { title: '副标题', key: 'subtitle', ellipsis: { tooltip: true } },
    { title: '排序', key: 'sort' }, { title: '状态', key: 'status', render: row => renderTag(row, 'status', tagMap.bannerStatus) },
    { title: '操作', key: 'actions', width: 140, fixed: 'right', render: row => renderActions(row, r => [{ key: 'ok', label: '编辑', fn: () => openEdit(r) }, { key: 'del', label: '删除', fn: () => del('banner', r.id) }]) }
  ]
  return []
})

// 编辑对话框
const editVis = ref(false), editTitle = ref(''), editDef = ref({}), ef = reactive({})
const upRef = ref(null), editId = ref(null)

// 各实体的编辑表单字段定义（字段类型、标签、选项等）
const defs = {
  member: { api: 'member', img: 'avatar', fields: [{ key: 'name', l: '姓名', t: 'input' }, { key: 'username', l: '用户名', t: 'input' }, { key: 'phone', l: '手机号', t: 'input' }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '正常', v: 1 }, { l: '停用', v: 0 }] }] },
  course: { api: 'course', img: 'imageUrl', fields: [{ key: 'name', l: '课程名称', t: 'input' }, { key: 'capacity', l: '容量', t: 'num', min: 1 }, { key: 'price', l: '价格', t: 'num', min: 0, prec: 2 }, { key: 'coachId', l: '教练', t: 'sel', opts: [] }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '上架', v: 1 }, { l: '下架', v: 0 }] }] },
  coach: { api: 'coach', img: 'avatar', fields: [{ key: 'name', l: '姓名', t: 'input' }, { key: 'specialty', l: '专长', t: 'input' }, { key: 'experience', l: '经验', t: 'input' }, { key: 'phone', l: '电话', t: 'input' }, { key: 'intro', l: '简介', t: 'area', rows: 3 }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '在职', v: 1 }, { l: '离职', v: 0 }] }] },
  card: { api: 'cardtype', img: 'coverImage', fields: [{ key: 'cardName', l: '卡种名称', t: 'input' }, { key: 'cardKey', l: '标识Key', t: 'input' }, { key: 'price', l: '价格', t: 'num', prec: 2 }, { key: 'validityDays', l: '有效期数量', t: 'num', min: 1 }, { key: 'validityUnit', l: '有效期单位', t: 'sel', opts: [{ l: '天', v: 'DAY' }, { l: '月', v: 'MONTH' }, { l: '年', v: 'YEAR' }] }, { key: 'sortOrder', l: '排序', t: 'num', min: 0 }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '上架', v: 1 }, { l: '下架', v: 0 }] }] },
  equipment: { api: 'equipment', img: 'imageUrl', fields: [{ key: 'name', l: '器材名称', t: 'input' }, { key: 'location', l: '位置', t: 'input' }, { key: 'remark', l: '备注', t: 'input' }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '正常', v: 1 }, { l: '维修中', v: 2 }, { l: '报废', v: 0 }] }] },
  banner: { api: 'banner', img: 'imageUrl', fields: [{ key: 'title', l: '标题', t: 'input' }, { key: 'subtitle', l: '副标题', t: 'input' }, { key: 'sort', l: '排序', t: 'num', min: 1 }, { key: 'status', l: '状态', t: 'sel', opts: [{ l: '启用', v: 1 }, { l: '禁用', v: 0 }] }] }
}

// 打开编辑/新增对话框：根据当前菜单加载对应表单定义
function openEdit(row) {
  const def = defs[active.value]; if (!def) return
  const coachField = def.fields.find(f => f.key === 'coachId')
  if (coachField) coachField.opts = coaches.value.map(c => ({ l: c.name, v: c.id }))
  editDef.value = def
  editTitle.value = row ? '编辑' : '添加'; editId.value = row ? row.id : null
  Object.keys(ef).forEach(k => delete ef[k])
  def.fields.forEach(f => { ef[f.key] = row ? (row[f.key] ?? '') : '' })
  if (def.img) ef[def.img] = row ? (row[def.img] || '') : ''
  editVis.value = true
}

const { onFileChange } = useFileUploader()

// 编辑对话框中的图片上传处理
function onUp(e) {
  onFileChange(e, url => { ef[editDef.value.img] = url })
}

// 各模块刷新函数映射（doSave 和 del 共用）
const refreshMap = { member: fMembers, course: fCourses, coach: fCoaches, card: fCardTypes, cardtype: fCardTypes, equipment: fEquipments, banner: fBanners, booking: fBookings, 'card-approval': fCards }

// 保存编辑/新增：根据是否有 editId 判断调用更新或新增接口
function doSave() {
  const def = editDef.value, p = { ...ef }
  if (p.coachId != null) p.coachId = Number(p.coachId)
  const req = editId.value ? request.put(`/${def.api}/update`, { ...p, id: editId.value }) : request.post(`/${def.api}/add`, p)
  req.then(r => {
    if (r.code === 200) {
      message.success('保存成功'); editVis.value = false
      refreshMap[active.value]?.(); fDashboard()
    } else message.error(r.msg || '保存失败')
  }).catch(() => message.error('请求失败，请检查网络或重新登录'))
}

// 通用确认弹窗（审批通过/拒绝、删除等都复用这个）
function confirmAction(content, action, refresh) {
  dialog.warning({
    title: '确认', content, positiveText: '确定', negativeText: '取消',
    onPositiveClick: () => action().then(r => {
      if (r.code === 200) { message.success('操作成功'); refresh?.(); fDashboard() }
      else message.error(r.msg || '操作失败')
    }).catch(() => message.error('请求失败，请检查网络'))
  })
}

// 删除操作
function del(api, id) {
  confirmAction('确定删除吗？', () => request.delete(`/${api}/${id}`), refreshMap[api])
}

// 数据导出：通过 fetch 带 token 请求后端，下载文件 blob 并触发保存
function doExport() {
  const k = active.value
  const urlMap = { booking: '/api/booking/export', 'card-approval': '/api/card/export', member: '/api/member/export' }
  const nameMap = { booking: '预约记录.xlsx', 'card-approval': '会员卡记录.csv', member: '会员信息.xlsx' }
  const url = urlMap[k]; if (!url) return
  const token = localStorage.getItem('token') || ''
  fetch(url, { headers: { Authorization: 'Bearer ' + token } })
    .then(r => r.blob())
    .then(blob => {
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = nameMap[k] || 'export'
      a.click()
      URL.revokeObjectURL(a.href)
    })
    .catch(() => message.error('导出失败'))
}

// 图表
const bChart = ref(null), iChart = ref(null); let bc = null, ic = null

function disposeCharts() { bc?.dispose(); ic?.dispose(); bc = null; ic = null }

// 初始化 ECharts 图表：课程预约柱状图 + 收入趋势折线图
function initCharts() {
  nextTick(() => {
    request.get('/stat/courseBooking').then(r => {
      if (r.code === 200 && bChart.value) {
        const d = r.data || []
        bc = echarts.init(bChart.value)
        bc.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 20, right: 20, bottom: 20, top: 20, containLabel: true }, xAxis: { type: 'category', data: d.map(v => v.courseName) }, yAxis: { type: 'value' }, series: [{ type: 'bar', data: d.map(v => v.count), itemStyle: { color: '#c9a96e' } }] })
      }
    }).catch(() => message.error('加载图表数据失败'))
    request.get('/stat/income').then(r => {
      if (r.code === 200 && iChart.value) {
        const d = r.data || {}
        ic = echarts.init(iChart.value)
        ic.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 20, right: 20, bottom: 20, top: 20, containLabel: true }, xAxis: { type: 'category', data: d.months || [] }, yAxis: { type: 'value' }, series: [{ type: 'line', smooth: true, data: d.income || [], itemStyle: { color: '#c9a96e' }, areaStyle: { color: 'rgba(201,169,110,0.1)' } }] })
      }
    }).catch(() => message.error('加载图表数据失败'))
  })
}

// 切换到仪表盘时自动刷新统计数据和图表
watch(active, v => { if (v === 'dashboard') { fDashboard(); disposeCharts(); initCharts() } })

// 设置
// 门店设置相关状态：基本配置、地址列表、地址编辑表单
const st = reactive({ hero_tag: '', brand_story: '', pay_qr: '', store_address: '', promo_video: '' }), suRefs = reactive({})
const addrs = ref([]), addrVis = ref(false), af = reactive({ address: '', phone: '', image: '' }), auRef = ref(null), edi = ref(-1)

// 加载门店配置和地址列表
function fetchSettings() {
  request.get('/config/all').then(r => {
    if (r.code === 200 && r.data) {
      Object.keys(st).forEach(k => st[k] = r.data[k] || '')
      try { const a = JSON.parse(r.data.store_address || '[]'); addrs.value = Array.isArray(a) ? a : [] } catch { addrs.value = [] }
    }
  }).catch(() => message.error('加载设置失败'))
}

// 设置页面的图片上传（收款码/视频）
function suUpload(e, key) {
  onFileChange(e, url => { st[key] = url; message.success('上传成功') })
}

// 保存门店设置
function saveSettings() { request.put('/config/save', { ...st }).then(r => { if (r.code === 200) message.success('保存成功') }).catch(() => message.error('保存失败')) }

// 地址对话框图片上传
function onAu(e) {
  onFileChange(e, url => { af.image = url })
}

// 地址新增/编辑/保存操作
function addAddr() { edi.value = -1; Object.assign(af, { address: '', phone: '', image: '' }); addrVis.value = true }

function editAddr(r, i) { edi.value = i; Object.assign(af, { address: r.address || '', phone: r.phone || '', image: r.image || '' }); addrVis.value = true }

function doSaveAddr() {
  if (!af.address) { message.warning('请填写地址'); return }
  if (edi.value >= 0) addrs.value[edi.value] = { ...af }
  else addrs.value.push({ ...af })
  addrVis.value = false; st.store_address = JSON.stringify(addrs.value); saveSettings()
}

// 门店地址表格列定义
const addrCols = [
  { title: '门店图片', key: 'image', width: 110, render: row => row.image ? h('img', { src: row.image, style: 'width:60px;height:40px;border-radius:4px;object-fit:cover' }) : '-' },
  { title: '地址', key: 'address', ellipsis: { tooltip: true } },
  { title: '电话', key: 'phone', width: 150 },
  { title: '操作', key: 'actions', width: 150, render: (row, idx) =>
    h('div', { style: 'display:flex;gap:8px' }, [
      h(NButton, { size: 'medium', type: 'primary', secondary: true, onClick: () => editAddr(row, idx) }, () => '编辑'),
      h(NButton, { size: 'medium', type: 'error', secondary: true, onClick: () => { addrs.value.splice(idx, 1); st.store_address = JSON.stringify(addrs.value); saveSettings() } }, () => '删除')
    ])
  }
]

// 初始化
// 退出管理后台
function logout() { ['token', 'username', 'name', 'avatar', 'role'].forEach(k => localStorage.removeItem(k)); router.push('/admin-login') }

// 页面挂载：启动时钟、加载管理员信息、拉取全部数据并初始化图表
onMounted(() => {
  now.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  nowTimer = setInterval(() => { now.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }, 30000)
  request.get('/admin/info').then(r => { if (r.code === 200) { admin.nickname = r.data.nickname || r.data.username || '管理员' } }).catch(() => message.error('管理员信息加载失败'))
  fDashboard(); fMembers(); fCourses(); fCoaches(); fBookings(); fEquipments(); fCardTypes(); fCards(); fetchSettings(); fBanners(); initCharts()
})

// 页面卸载：清理时钟定时器、图表实例
onUnmounted(() => { if (nowTimer) clearInterval(nowTimer); disposeCharts() })
</script>

<style scoped>
/* ====== 管理后台亮色主题 — 高对比度 ====== */
.admin-page {
  --abg: #f5f5f7;
  --acard: #ffffff;
  --aside-bg: #1c1c22;
  --border: #e5e5ea;
  --text: #1c1c1e;
  --text-secondary: #6e6e73;
  --text-muted: #aeaeb2;
  --stat-blue-bg: #eff6ff;
  --stat-green-bg: #f0fdf4;
  --stat-purple-bg: #faf5ff;
  --stat-amber-bg: #fffbeb;
  --stat-rose-bg: #fff1f2;
  --tag-success-bg: #dcfce7;
  --tag-success-text: #15803d;
  --tag-warning-bg: #fef3c7;
  --tag-warning-text: #b45309;
  --tag-error-bg: #fee2e2;
  --tag-error-text: #b91c1c;
  --tag-info-bg: #f1f5f9;
  --tag-info-text: #475569;
  --table-stripe: #fafafa;
  --table-hover: #fdf6ed;
  --table-border: #f0f0f4;
  background: var(--abg);
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.admin-layout { height: 100vh; }

/* ====== 侧边栏 — 暗色，高对比度 ====== */
.aside-brand {
  padding: 22px 20px;
  font-size: 18px; font-weight: 800;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: var(--aside-bg);
}
.menu { flex: 1; padding: 8px 0; }
.aside-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: var(--aside-bg);
}
.aside-user { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.aside-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700; color: #1c1c22;
  background: #c9a96e;
}
.aside-name { font-size: 15px; font-weight: 600; color: #e4e4e7; }
.aside-role { font-size: 13px; color: rgba(255, 255, 255, 0.3); }

:deep(.n-menu .n-menu-item) {
  font-size: 16px !important;
  padding: 14px 20px !important;
  height: auto !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
  }

/* ====== 头部 ====== */
.main-header {
  padding: 20px 28px;
  display: flex; justify-content: space-between; align-items: center;
  background: var(--acard);
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 50;
}
.main-header h2 {
  font-size: 20px; font-weight: 800; color: var(--text); margin: 0;
}
.main-clock { font-size: 15px; color: var(--text-muted); }
.main-body { padding: 24px; }

/* ====== 统计卡片 — 彩色图标背景 ====== */
.stat-card {
  display: flex; align-items: center; gap: 16px;
  padding: 6px 0;
}
.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
}
.stat-row > div:nth-child(1) .stat-icon { background: var(--stat-blue-bg); }
.stat-row > div:nth-child(2) .stat-icon { background: var(--stat-green-bg); }
.stat-row > div:nth-child(3) .stat-icon { background: var(--stat-purple-bg); }
.stat-row > div:nth-child(4) .stat-icon { background: var(--stat-amber-bg); }
.stat-row > div:nth-child(5) .stat-icon { background: var(--stat-rose-bg); }
.stat-val {
  font-size: 32px; font-weight: 800; color: var(--text); line-height: 1.1;
}
.stat-label {
  font-size: 14px; color: var(--text-secondary); margin-top: 2px;
}

/* ====== 表格 ====== */
.table-toolbar {
  display: flex; justify-content: space-between; align-items: center; width: 100%;
}
.table-title { font-size: 17px; font-weight: 700; color: var(--text); }
.table-actions { display: flex; gap: 12px; }

:deep(.n-card) {
  background: var(--acard) !important;
  border: 1px solid var(--border) !important;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04) !important;
}
:deep(.n-card > .n-card-header) {
  border-bottom: 1px solid var(--border) !important;
}
:deep(.n-card > .n-card-header .n-card-header__main) {
  color: var(--text) !important;
}

:deep(.n-data-table) {
  --n-td-color: #fff !important;
  --n-td-color-striped: var(--table-stripe) !important;
  --n-td-color-hover: var(--table-hover) !important;
  --n-border-color: var(--table-border) !important;
  --n-th-color: #f9f9fb !important;
  --n-th-text-color: var(--text) !important;
  --n-td-text-color: var(--text) !important;
  font-size: 15px !important;
}
:deep(.n-data-table table) {
  table-layout: auto !important;
}
:deep(.n-data-table .n-data-table-th) {
  font-weight: 700 !important;
  font-size: 14px !important;
  color: var(--text-secondary) !important;
}

/* ====== 高对比度标签 ====== */
:deep(.n-tag.n-tag--success-type) {
  background: var(--tag-success-bg) !important;
  color: var(--tag-success-text) !important;
  border: none !important;
  font-weight: 600 !important;
}
:deep(.n-tag.n-tag--warning-type) {
  background: var(--tag-warning-bg) !important;
  color: var(--tag-warning-text) !important;
  border: none !important;
  font-weight: 600 !important;
}
:deep(.n-tag.n-tag--error-type) {
  background: var(--tag-error-bg) !important;
  color: var(--tag-error-text) !important;
  border: none !important;
  font-weight: 600 !important;
}
:deep(.n-tag.n-tag--info-type) {
  background: var(--tag-info-bg) !important;
  color: var(--tag-info-text) !important;
  border: none !important;
  font-weight: 600 !important;
}

/* ====== 亮色模式下的按钮 ====== */
:deep(.n-button.n-button--primary-type) {
  background: #c9a96e !important;
  color: #fff !important;
  border-color: #c9a96e !important;
}
:deep(.n-button.n-button--primary-type:hover) {
  background: #b8944e !important;
}

/* ====== 字段 ====== */
.s-img {
  width: 80px; height: 80px; border-radius: 50%;
  border: 2px dashed #d4d4d8;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; overflow: hidden; transition: border-color .2s;
}
.s-img:hover { border-color: #c9a96e; }
.s-img-inner { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
.s-img-empty { font-size: 14px; color: var(--text-muted); }
/* 首页视频预览区域 */
.s-video {
  width: 240px; height: 135px; border-radius: 8px;
  border: 2px dashed #d4d4d8;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; overflow: hidden; transition: border-color .2s;
}
.s-video:hover { border-color: #c9a96e; }
.s-video-inner { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }

/* ====== 响应式 ====== */
@media (max-width: 1200px) {
  :deep(.n-data-table) { font-size: 13px !important; }
  :deep(.n-data-table .n-data-table-th) { font-size: 12px !important; }
  :deep(.n-data-table .n-data-table-td) { padding: 8px 10px !important; }
  :deep(.n-data-table .n-data-table-th) { padding: 8px 10px !important; }
}

/* 对话框响应式：防止小屏溢出 */
:deep(.n-modal) {
  max-width: 90vw;
}
/* 侧边栏折叠时隐藏文字内容 */
:deep(.n-layout-sider.n-layout-sider--collapsed) .aside-brand {
  display: none;
}
:deep(.n-layout-sider.n-layout-sider--collapsed) .aside-footer {
  padding: 8px 4px;
}
:deep(.n-layout-sider.n-layout-sider--collapsed) .aside-user > div {
  display: none;
}
:deep(.n-layout-sider.n-layout-sider--collapsed) .aside-user {
  justify-content: center;
}
</style>