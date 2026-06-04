<template>
  <div class="login-left">
    <img src="/images/gym5.jpg" class="left-bg" />
    <div class="left-overlay" />
    <div class="left-content">
      <b class="left-brand">GYMCORE</b>
      <div>
        <h1>雕刻身体<br/>定义生活</h1>
        <p>专业健身连锁品牌 · 科学训练 · 顶级教练</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-left { flex: 1; position: relative; overflow: hidden; }
.left-bg { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; filter: saturate(0.4) brightness(0.5); }
.left-overlay { position: absolute; inset: 0; background: linear-gradient(160deg, rgba(20, 12, 5, 0.92), rgba(40, 22, 8, 0.7), rgba(10, 5, 3, 0.92)); }
.left-content { position: relative; z-index: 1; height: 100%; padding: 48px; display: flex; flex-direction: column; justify-content: space-between; }
.left-brand { font-size: var(--text-xl); font-weight: 800; letter-spacing: 3px; color: #fff; }
.left-content h1 { font-size: clamp(36px, 5vw, 60px); font-weight: 900; line-height: 1.05; letter-spacing: -1px; color: #fff; margin-bottom: 16px; }
.left-content p { font-size: var(--text-base); letter-spacing: 2px; color: rgba(201, 169, 110, 0.55); font-weight: 300; }
@media (max-width: 768px) { .login-left { height: 220px; flex: none; } .left-content h1 { font-size: 28px; } }
</style>
