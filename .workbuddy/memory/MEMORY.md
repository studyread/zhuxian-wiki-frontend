# 项目记忆

## 诛仙世界Wiki项目

### 项目位置
- 后端: `F:\诛仙世界wiki\zhuxian-wiki-backend\`
- 前端: `F:\诛仙世界wiki\zhuxian-wiki-frontend\`

### 技术栈
- 后端: Spring Boot + MyBatis-Plus + MySQL + Redis
- 前端: Vue 3 + Vite + Axios
- AI: 通义千问 API

### 项目状态
- ✅ 完成前后端 API 对接
- ✅ 完成古风游戏 Wiki 风格改版
- ✅ 所有二级页面已完善
- ✅ 知识库模块已完成
- ✅ AI问答知识库约束已实现
- ✅ 管理员页面已完成

### 页面清单
| 页面 | 文件 | 状态 |
|------|------|------|
| 首页 | Home.vue | ✅ |
| 文章详情 | Article.vue | ✅ |
| 文章编辑 | ArticleEdit.vue | ✅ |
| 分类页 | Category.vue | ✅ |
| 搜索页 | Search.vue | ✅ |
| AI聊天 | AIChat.vue | ✅ |
| 用户中心 | User.vue | ✅ |
| 关于我们 | About.vue | ✅ |
| 404页面 | NotFound.vue | ✅ |
| 管理员登录 | AdminLogin.vue | ✅ |
| 管理员仪表盘 | AdminDashboard.vue | ✅ |
| 知识库管理 | KnowledgeManage.vue | ✅ |
| 知识录入 | KnowledgeEdit.vue | ✅ |
| 分类管理 | CategoryManage.vue | ✅ |
| 操作日志 | LogManage.vue | ✅ |

### 知识库模块
- 数据库表: knowledge_category, knowledge_entry, knowledge_index, knowledge_log, admin
- 后端API: 4个Controller
- 前端页面: 7个组件
- AI提示词: `resources/prompt/SYSTEM_PROMPT.md`

### 已修复的Bug
1. AIService: keyword 为 null 导致空指针 → 添加空值检查
2. ChatHistoryMapper: content 字段插入失败 → 添加自定义 insertWithContent
3. AIChat.vue: 请求参数名错误 (message→question) 和响应取值错误 (reply→data.answer)

### 风格指南
- 古风游戏 Wiki 风格: 白底黑字、水墨边框、古典配色
- 主色调: 朱砂红 #c45c48
- 背景色: 古风纸张色 #faf8f3
- 字体: 中文衬线体标题 + 无衬线正文

### UI优化记录 (2026-04-25)
**后台管理页面风格升级：**

1. **KnowledgeEdit.vue (添加词条)**
   - 卡片分组布局：基本信息、内容编辑、附加信息、图片上传四张卡片
   - 左右分栏+实时预览功能
   - 编辑器工具栏添加SVG图标，支持更多Markdown语法（表格、任务列表、分割线）
   - 图片上传区域优化，支持预览和快速插入

2. **CategoryManage.vue (分类管理)**
   - 分类卡片添加图标背景色
   - 图标预设快速选择器
   - 操作按钮添加SVG图标
   - 弹窗表单优化

3. **Settings.vue (系统设置)**
   - 左侧Tab导航替代顶部Tab
   - 分组卡片式内容布局
   - 颜色预设快捷选择
   - 单选/开关/分段控件样式优化

4. **LogManage.vue (操作日志)**
   - 统计信息改为卡片式布局
   - 筛选改为Tab切换方式
   - 日志卡片样式优化
   - 分页控件样式升级

### 管理后台路由方案 (2026-04-25 最终)
- 项目真实目录：`F:\诛仙世界wiki\zhuxian-wiki-frontend\`（非 workspace 副本）
- 独立入口：admin.html + admin-main.js + admin-router.js + AdminApp.vue
- **使用 hash 路由**（createWebHashHistory），解决刷新 404 问题
- 后台入口：`http://localhost:3000/admin.html`（自动路由）
- hash 路径示例：`admin.html#/dashboard`、`admin.html#/login`、`admin.html#/knowledge`
- WikiHeader.vue goToAdmin：`window.location.href = '/admin.html'`
- AdminLayout.vue 退出：`window.location.href = '/admin.html#/login'`
- 登录 token 存储 key：`admin_token`
- 默认账户：admin / admin123

### Redis 安装配置 (2026-05-02)
- 安装位置：`C:\Redis\`
- Redis 版本：5.0.14.1（x64）
- 启动命令：`C:\Redis\redis-server.exe C:\Redis\redis.windows.conf`
- 快捷启动：`C:\Redis\启动Redis.bat`
- 客户端测试：`C:\Redis\redis-cli.exe ping`（返回 PONG 表示正常）
- 配置参数：localhost:6379，密码为空，数据库 0，最大内存 256MB
- 已添加到用户 PATH 环境变量
