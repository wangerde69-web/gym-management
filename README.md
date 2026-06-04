# Gym Management System — 健身房管理系统

基于 Spring Boot 3 + Vue 3 的全栈健身房管理平台，提供前台展示、会员预约、后台管理及 AI 体态分析等功能。

## 技术栈

**后端**
- Java 17 + Spring Boot 3.2
- MyBatis-Plus 3.5（ORM）
- MySQL 8.0（数据库）
- Spring Security（认证授权）
- JWT（jjwt 0.12，令牌鉴权）
- Apache POI 5.2（Excel/CSV 导出）

**前端**
- Vue 3 + Vite 5
- Naive UI 2.42（组件库）
- Vue Router 4（路由）
- Axios（HTTP 请求）
- ECharts 5（数据图表）
- Anime.js（动画）

**AI 能力**
- YOLOv8n-Pose 姿态识别模型
- Python 体态分析脚本
- 支持本地 LLM 接口扩展

## 功能模块

### 前台展示（无需登录）
- 首页轮播图、课程/教练/器材展示
- 课程详情与教练详情
- 会员注册与登录

### 会员功能（需登录）
- 课程预约与取消
- 会员卡购买与查看
- 个人预约记录
- AI 体态分析（上传照片）

### 管理后台（需管理员登录）
- **数据看板**：办卡人数、课程/教练/器材/预约统计图表
- **会员管理**：列表展示、搜索、新增/编辑/删除、Excel 导出
- **课程管理**：CRUD、绑定教练、上下架
- **教练管理**：CRUD、状态管理
- **会员卡管理**：卡种配置、审批流程、CSV 导出
- **器材管理**：CRUD
- **轮播图管理**：图片上传、排序
- **预约管理**：详情查看、导出 Excel
- **门店设置**：品牌信息、收款码、地址管理

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- （可选）Python 3.9+ 用于体态分析

### 1. 数据库

```sql
-- 创建数据库
CREATE DATABASE gym DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
-- 导入表结构与数据
SOURCE SQL/gym.sql;
```

### 2. 后端启动

```bash
cd backend

# 修改数据库连接（src/main/resources/application.yml）
# spring.datasource.username / password

# 启动
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8081`。

### 3. 前端启动

```bash
cd frontend

npm install
npm run dev
```

前端默认运行在 `http://localhost:3000`，已配置 API 代理到后端。

### 4. 体态分析（可选）

```bash
cd backend/python
pip install -r requirements.txt
python pose_analyzer.py
```

## 项目结构

```
├── SQL/                    # 数据库脚本
├── backend/                # 后端（Spring Boot）
│   ├── src/
│   │   └── main/
│   │       ├── java/com/gym/
│   │       │   ├── config/      # 安全、JWT、跨域等配置
│   │       │   ├── controller/  # REST 控制器
│   │       │   ├── entity/      # 实体类
│   │       │   ├── mapper/      # MyBatis-Plus Mapper
│   │       │   └── service/     # 业务逻辑
│   │       └── resources/       # 配置文件 & Mapper XML
│   └── pom.xml
├── frontend/               # 前端（Vue 3）
│   ├── src/
│   │   ├── api/            # API 请求封装
│   │   ├── assets/         # 全局样式
│   │   ├── components/     # 公共组件
│   │   ├── router/         # 路由配置
│   │   └── views/          # 页面组件
│   └── package.json
└── README.md
```

## API 概览

| 模块       | 前缀               | 说明                   |
|-----------|-------------------|----------------------|
| 前台      | `/api/front`      | 首页聚合、课程/教练详情 |
| 认证      | `/api/auth`       | 登录、注册             |
| 会员      | `/api/member`     | 会员 CRUD、导出         |
| 课程      | `/api/course`     | 课程 CRUD               |
| 教练      | `/api/coach`      | 教练 CRUD               |
| 器材      | `/api/equipment`  | 器材 CRUD               |
| 预约      | `/api/booking`    | 预约管理、导出          |
| 会员卡    | `/api/card`       | 购卡、审批、导出        |
| 卡种      | `/api/cardtype`   | 卡种配置                |
| 轮播图    | `/api/banner`     | 轮播 CRUD               |
| 文件      | `/api/file`       | 文件上传                |
| 配置      | `/api/config`     | 门店设置                |
| 统计      | `/api/stat`       | 仪表盘数据、图表        |
| 体态分析  | `/api/pose`       | 图片上传分析            |

## 默认账号

**前台用户**：自行注册

**管理员**：
- 用户名：`admin`
- 密码：`123456`