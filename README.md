# 诛仙世界 Wiki

一个基于 Spring Boot + Vue 3 的游戏攻略 Wiki 系统，支持 AI 智能问答。

## 项目结构

```
诛仙世界wiki/
├── zhuxian-wiki-backend/      # 后端 (Spring Boot 3.2 + JDK 17)
│   ├── src/main/java/
│   │   └── com/zhuxianwiki/
│   │       ├── controller/   # REST API 控制器
│   │       ├── service/      # 业务逻辑层
│   │       ├── mapper/       # 数据访问层
│   │       ├── entity/       # 实体类
│   │       ├── config/       # 配置类
│   │       └── AI/           # AI 服务
│   └── src/main/resources/
│       ├── mapper/           # MyBatis XML
│       └── application.yml   # 配置文件
│
├── zhuxian-wiki-frontend/    # 前端 (Vue 3 + Element Plus)
│   ├── src/
│   │   ├── components/       # Vue 组件
│   │   ├── views/           # 页面视图
│   │   ├── api/             # API 接口
│   │   ├── router/          # 路由配置
│   │   └── styles/          # 样式文件
│   └── package.json
│
└── database/
    └── init.sql             # 数据库初始化脚本
```

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis (可选，用于缓存)

## 快速开始

### 1. 数据库配置

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本
source F:/诛仙世界wiki/database/init.sql
```

### 2. 启动后端

```bash
# 进入后端目录
cd F:/诛仙世界wiki/zhuxian-wiki-backend

# 使用 IDEA 打开项目
# IDEA 会自动下载 Maven 依赖

# 修改 src/main/resources/application.yml 中的数据库密码

# 运行 ZhuxianWikiApplication.java 启动后端
# 访问 http://localhost:8080
```

### 3. 启动前端

```bash
# 进入前端目录
cd F:/诛仙世界wiki/zhuxian-wiki-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:3000
```

## 配置说明

### 后端配置 (application.yml)

```yaml
spring:
  datasource:
    password: root  # 修改为你的 MySQL 密码

ai:
  api-key: your-api-key-here  # 填写你的 AI API Key (硅基流动)
  model: deepseek-ai/DeepSeek-V2.5
```

### AI 配置

项目使用硅基流动 API，接入 DeepSeek 模型：
1. 注册 https://siliconflow.cn
2. 获取 API Key
3. 填入配置文件

## 功能特性

- ✅ Wiki 文章管理 (CRUD)
- ✅ 分类系统 (门派/副本/装备/攻略)
- ✅ 热门/最新文章推荐
- ✅ 全文搜索
- ✅ AI 智能问答
- ✅ 用户登录注册
- ✅ Redis 缓存
- ✅ 响应式布局

## 技术栈

### 后端
- Spring Boot 3.2
- MyBatis Plus 3.5
- MySQL 8.0
- Redis
- JWT (jjwt 0.12.x)

### 前端
- Vue 3
- Vue Router 4
- Pinia
- Element Plus
- Axios

## 默认账号

- 用户名: admin
- 密码: admin123

## 截图预览

首页采用 BiliGame Wiki 风格：
- 左侧固定导航栏
- 三栏式布局
- 暗色主题
- 卡片式文章展示

## 常见问题

### 1. 启动报错找不到主类
确保使用 JDK 17+，Spring Boot 3.x 需要 JDK 17

### 2. 前端无法请求后端
检查后端是否启动在 8080 端口，确认跨域配置正确

### 3. AI 功能不可用
检查 AI API Key 是否正确配置，网络是否正常

## License

MIT
