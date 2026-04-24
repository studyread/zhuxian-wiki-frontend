# 诛仙世界 Wiki 项目工作记录

## 📅 日期
2026-04-23

## 🚀 今日完成工作

### 1. 知识库模块 ✅

#### 后端实现
| 文件 | 说明 |
|------|------|
| `sql/knowledge_base.sql` | 知识库数据库表结构 |
| `entity/KnowledgeCategory.java` | 知识库分类实体 |
| `entity/KnowledgeEntry.java` | 知识库词条实体 |
| `entity/KnowledgeIndex.java` | 知识库索引实体 |
| `entity/KnowledgeLog.java` | 知识库日志实体 |
| `entity/Admin.java` | 管理员实体 |
| `mapper/*` | 5个Mapper接口 |
| `mapper/*.xml` | 2个Mapper XML配置 |
| `service/*` | 5个Service类 |
| `controller/KnowledgeCategoryController.java` | 分类管理API |
| `controller/KnowledgeEntryController.java` | 词条管理API |
| `controller/KnowledgeLogController.java` | 日志查询API |
| `controller/AdminController.java` | 管理员API |

#### 前端实现
| 文件 | 说明 |
|------|------|
| `api/knowledge.js` | 知识库API封装 |
| `views/admin/AdminLogin.vue` | 管理员登录页 |
| `views/admin/AdminLayout.vue` | 管理员布局 |
| `views/admin/AdminDashboard.vue` | 管理员仪表盘 |
| `views/admin/KnowledgeManage.vue` | 知识库管理 |
| `views/admin/KnowledgeEdit.vue` | 知识录入/编辑 |
| `views/admin/CategoryManage.vue` | 分类管理 |
| `views/admin/LogManage.vue` | 操作日志 |
| `views/admin/Settings.vue` | 系统设置 |
| `router/index.js` | 路由配置更新 |

### 2. AI问答知识库约束 ✅

#### 提示词配置
- `resources/prompt/SYSTEM_PROMPT.md` - AI系统提示词文档
- 核心约束：
  - 知识库优先原则
  - 诚实回答原则（知识库无内容时如实告知）
  - 禁止胡编乱造
  - Markdown格式化回答

#### AIService更新
- 集成 `KnowledgeEntryMapper`
- 优先从知识库检索
- 无相关内容时如实告知用户

### 3. 用户模块完善 ✅

#### 后端API
- `UserController` - 用户登录/注册/信息API
- `UserService` - 用户业务逻辑

#### 前端页面
- `User.vue` - 用户中心页面
  - 登录弹窗
  - 注册弹窗
  - 意见反馈弹窗
  - 用户菜单

### 4. 管理员模块完善 ✅

#### 后端
- `AdminController` - 管理员登录API
- `AdminService` - 管理员业务逻辑

#### 前端
- 7个管理员页面组件
- 完整的路由配置

---

## 📊 数据库表结构

### 知识库分类表 (knowledge_category)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名称 |
| description | VARCHAR(255) | 分类描述 |
| icon | VARCHAR(20) | 分类图标 |
| sort_order | INT | 排序 |
| status | TINYINT | 状态 |

### 知识库词条表 (knowledge_entry)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(200) | 词条标题 |
| summary | VARCHAR(500) | 摘要 |
| content | LONGTEXT | 内容(Markdown) |
| category_id | BIGINT | 分类ID |
| tags | VARCHAR(500) | 标签 |
| status | TINYINT | 状态 |
| source_url | VARCHAR(500) | 来源URL |

### 管理员表 (admin)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(200) | 密码(BCrypt加密) |
| nickname | VARCHAR(50) | 昵称 |
| role | VARCHAR(20) | 角色 |

---

## 🎯 下一步工作

1. 执行 `knowledge_base.sql` 创建数据库表
2. 重启后端服务
3. 测试知识库功能
4. 添加更多知识库词条
5. 测试AI问答约束

---

## 📝 备注

- 前端图片 `big_logo.webp` 已复制到 `public/` 目录
- 所有"典"字图标已替换为图片
- AI提示词文档位于 `resources/prompt/SYSTEM_PROMPT.md`
