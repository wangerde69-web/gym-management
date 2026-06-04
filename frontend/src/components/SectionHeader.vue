<!-- 区块标题组件：展示标签(tag)、主标题(title)和副标题(subtitle)，支持居中布局 -->
<template>
  <div class="section-header" :class="{ centered }">
    <span v-if="tag" class="sec-tag">{{ tag }}</span>
    <h2>{{ title }}</h2>
    <p v-if="subtitle">{{ subtitle }}</p>
  </div>
</template>

<script setup>
defineProps({
  tag: String,
  title: { type: String, required: true },
  subtitle: String,
  centered: Boolean
})
</script>

<style scoped>
/* 区块标题基础样式和居中变体 */
.section-header { margin-bottom: 48px; }
.section-header.centered { text-align: center; }
/* 标签（英文大写装饰文字） */
.sec-tag {
  display: inline-block;
  font-size: var(--text-xs);
  letter-spacing: 4px;
  color: var(--gold);
  font-weight: 600;
  text-transform: uppercase;
  margin-bottom: 16px;
}
/* 主标题 */
.section-header h2 {
  font-size: var(--text-2xl);
  font-weight: 800;
  color: #fff;
  margin-bottom: 10px;
  letter-spacing: -0.5px;
}
/* 副标题描述文字 */
.section-header p {
  font-size: var(--text-base);
  color: var(--text-muted);
  font-weight: 400;
  max-width: 520px;
}
.centered p { margin: 0 auto; }
</style>