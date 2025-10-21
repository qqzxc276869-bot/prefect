# 实验室化学试剂与耗材库存管理系统

## 项目简介

基于 SpringBoot + Vue + MySQL 的实验室化学试剂与耗材库存管理系统，支持三种用户角色（学生、老师、管理员），实现试剂的库存管理、申领审批、出入库管理等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.14
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.3.1
- **权限**: JWT (JSON Web Token)
- **工具**: Lombok, Hutool

### 前端
- **框架**: Vue 2.6.14
- **UI组件**: Element UI 2.15.13
- **状态管理**: Vuex
- **路由**: Vue Router
- **HTTP客户端**: Axios

## 功能模块

### 1. 学生/普通用户
- **库存查询**: 查看试剂名称、规格、剩余库存、存放位置
- **在线申领**: 提交领用申请，填写试剂名称、数量和用途
- **查看状态**: 查看自己提交的申请的处理进度（待审核/已通过/已拒绝）

### 2. 老师/仓库管理员
- **入库登记**: 新试剂到货后，录入名称、规格、数量、有效期、存放位置等信息
- **审批与出库**: 审批学生提交的领用申请，执行出库操作，系统自动扣减库存
- **库存维护**: 手动更新库存，设置库存下限预警值
- **预警查看**: 查看库存不足和试剂临期的预警列表
- **出入库记录**: 查看完整的出入库历史记录

### 3. 系统管理员
- **用户管理**: 管理系统用户账户（添加、禁用学生和老师账户）
- **数据查看**: 查看实验室库存总览、消耗记录、预警统计等数据报表
- **基础信息维护**: 管理试剂分类、实验室房间位置等基础字典信息
- **注**: 管理员不具有出入库的功能，专注于系统管理和监督

## 数据库设计

系统包含以下主要数据表：

1. **sys_user**: 用户表
2. **reagent**: 试剂信息表
3. **reagent_category**: 试剂分类表
4. **storage_location**: 存放位置表
5. **inventory**: 库存表
6. **stock_in_record**: 入库记录表
7. **stock_out_record**: 出库记录表
8. **application**: 领用申请表

详细的数据库设计请查看 `database/reagent_management.sql`

## 项目结构

```
reagent-management/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lab/reagent/
│   │   │   │   ├── common/         # 通用类
│   │   │   │   ├── config/         # 配置类
│   │   │   │   ├── controller/     # 控制器
│   │   │   │   ├── dto/            # 数据传输对象
│   │   │   │   ├── entity/         # 实体类
│   │   │   │   ├── mapper/         # Mapper接口
│   │   │   │   ├── service/        # 服务类
│   │   │   │   ├── util/           # 工具类
│   │   │   │   └── vo/             # 视图对象
│   │   │   └── resources/
│   │   │       └── application.yml # 配置文件
│   └── pom.xml                     # Maven配置
├── frontend/                   # 前端项目
│   ├── public/
│   ├── src/
│   │   ├── api/                    # API接口
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 组件
│   │   ├── router/                 # 路由
│   │   ├── store/                  # 状态管理
│   │   ├── utils/                  # 工具类
│   │   ├── views/                  # 页面
│   │   │   ├── Login.vue           # 登录页
│   │   │   ├── Student/            # 学生端
│   │   │   ├── Teacher/            # 老师端
│   │   │   └── Admin/              # 管理员端
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vue.config.js
├── database/                   # 数据库脚本
│   └── reagent_management.sql
└── README.md                   # 项目说明
```

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 14+
- npm 6+

### 1. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行数据库脚本
source database/reagent_management.sql
```

### 2. 后端启动

```bash
# 进入后端目录
cd backend

# 修改 src/main/resources/application.yml 中的数据库配置
# 配置你的MySQL用户名和密码

# Maven构建
mvn clean install

# 启动项目
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 3. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端应用将在 `http://localhost:8081` 启动

### 4. 访问系统

打开浏览器访问 `http://localhost:8081`

## 默认账户

系统提供了三个测试账户：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 学生 | student | 123456 | 可以查询库存和提交申请 |
| 老师 | teacher | 123456 | 可以管理库存、审批申请、出入库 |
| 管理员 | admin | 123456 | 可以管理用户和基础数据 |

## 核心功能演示

### 学生端功能
1. **库存查询**: 支持按试剂名称搜索，查看详细库存信息
2. **试剂申领**: 选择试剂、填写数量和用途，提交申请
3. **我的申请**: 查看申请历史和审批状态

### 老师端功能
1. **库存管理**: 查看和管理所有库存，设置预警阈值
2. **入库登记**: 录入新到货试剂信息
3. **申请审批**: 审批学生的领用申请（通过/拒绝）
4. **出库管理**: 执行出库操作，支持关联申请单
5. **库存预警**: 自动提醒库存不足和即将过期的试剂
6. **出入库记录**: 完整的历史记录追溯

### 管理员端功能
1. **数据总览**: 展示系统关键指标（用户数、库存数、预警数、申请数）
2. **用户管理**: 添加、编辑、启用/禁用用户
3. **库存查看**: 查看全局库存状态
4. **申请记录**: 查看所有申请记录
5. **基础数据**: 管理试剂分类和存放位置

## 系统特色

### 1. 权限分离
- 三种角色各司其职，管理员专注系统管理，不参与实际出入库操作
- 基于JWT的无状态认证，安全可靠

### 2. 智能预警
- 自动检测库存不足（低于预警阈值）
- 自动检测试剂即将过期（提前30天预警）
- 自动检测试剂已过期

### 3. 流程规范
- 完整的申请→审批→出库流程
- 所有操作留有记录，可追溯

### 4. 用户友好
- 现代化的UI设计
- 直观的操作流程
- 实时的状态反馈

## API接口文档

### 认证接口
- `POST /api/auth/login` - 用户登录

### 用户管理
- `GET /api/user/list` - 查询用户列表
- `POST /api/user/add` - 添加用户
- `PUT /api/user/update` - 更新用户
- `PUT /api/user/status/{id}/{status}` - 更新用户状态
- `DELETE /api/user/delete/{id}` - 删除用户

### 库存管理
- `GET /api/inventory/list` - 查询库存列表
- `GET /api/inventory/warning` - 查询预警列表
- `PUT /api/inventory/threshold/{id}` - 更新预警阈值

### 申请管理
- `POST /api/application/submit` - 提交申请
- `GET /api/application/my` - 查询我的申请
- `GET /api/application/pending` - 查询待审批申请
- `GET /api/application/all` - 查询所有申请
- `POST /api/application/review/{id}` - 审批申请

### 出入库管理
- `POST /api/stock/in` - 入库
- `POST /api/stock/out` - 出库
- `GET /api/stock/in/list` - 入库记录查询
- `GET /api/stock/out/list` - 出库记录查询

### 试剂管理
- `GET /api/reagent/list` - 查询试剂列表
- `POST /api/reagent/add` - 添加试剂
- `PUT /api/reagent/update` - 更新试剂
- `DELETE /api/reagent/delete/{id}` - 删除试剂

### 基础数据
- `GET /api/base/category/list` - 查询分类列表
- `POST /api/base/category/add` - 添加分类
- `DELETE /api/base/category/delete/{id}` - 删除分类
- `GET /api/base/location/list` - 查询位置列表
- `POST /api/base/location/add` - 添加位置
- `DELETE /api/base/location/delete/{id}` - 删除位置

## 开发说明

### 后端开发

1. 实体类使用 Lombok 简化代码
2. 使用 MyBatis Plus 简化CRUD操作
3. 统一的 Result 返回格式
4. 全局异常处理
5. 跨域配置支持前后端分离

### 前端开发

1. 使用 Element UI 组件库
2. Axios 封装统一请求拦截
3. Vuex 管理用户状态
4. Vue Router 实现路由守卫
5. 响应式布局设计

## 注意事项

1. **安全性**:
   - 生产环境请修改 JWT 密钥（application.yml 中的 jwt.secret）
   - 建议使用更强的密码策略
   - 定期备份数据库

2. **性能优化**:
   - 大量数据时建议添加分页功能
   - 可以添加 Redis 缓存提升性能

3. **功能扩展**:
   - 可添加导出Excel功能
   - 可添加试剂使用统计图表
   - 可添加消息通知功能

## 常见问题

### 1. 后端启动失败
- 检查数据库是否正常运行
- 检查 application.yml 中的数据库配置是否正确
- 确认 MySQL 端口（默认3306）是否被占用

### 2. 前端无法访问后端
- 检查后端是否成功启动
- 检查 vue.config.js 中的代理配置
- 查看浏览器控制台是否有 CORS 错误

### 3. 登录失败
- 确认数据库中有初始用户数据
- 检查密码是否正确（默认都是 123456）
- 查看后端控制台日志

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，欢迎提交 Issue。

---

**祝您使用愉快！**






