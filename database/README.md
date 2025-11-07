# 数据库初始化说明

## 目录结构

- `init_database.sql` - 完整的数据库初始化脚本（建表+初始数据）
- `init_db.bat` - Windows批处理脚本，用于快速执行初始化
- `README.md` - 本说明文档

## 快速开始

### 方法一：使用批处理脚本（推荐）

1. **确保MySQL服务已启动**
   
2. **修改数据库连接参数（如需要）**
   
   编辑 `init_db.bat` 文件，修改以下参数：
   ```batch
   set MYSQL_HOST=127.0.0.1
   set MYSQL_PORT=3306
   set MYSQL_USER=root
   set MYSQL_PASSWORD=root
   ```

3. **运行初始化脚本**
   
   双击运行 `init_db.bat` 文件，或在命令行中执行：
   ```bash
   cd database
   init_db.bat
   ```

### 方法二：使用MySQL命令行

1. **打开命令行工具**

2. **登录MySQL**
   ```bash
   mysql -u root -p
   ```

3. **执行初始化脚本**
   ```sql
   source /path/to/database/init_database.sql
   ```
   
   或者在命令行直接执行：
   ```bash
   mysql -u root -p < init_database.sql
   ```

### 方法三：使用MySQL Workbench等图形化工具

1. 打开 MySQL Workbench
2. 连接到MySQL服务器
3. 打开 `init_database.sql` 文件
4. 点击执行按钮运行脚本

## 初始化内容

### 数据库信息

- **数据库名**: `reagent_management`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`

### 数据表（共31张表）

#### 核心业务表
1. `sys_user` - 系统用户表
2. `reagent_category` - 试剂分类表
3. `reagent` - 试剂基础信息表
4. `storage_location` - 存放位置表
5. `inventory` - 库存表
6. `stock_in_record` - 入库记录表
7. `stock_out_record` - 出库记录表
8. `application` - 领用申请表

#### 课题组管理表
9. `research_group` - 课题组表
10. `research_group_member` - 课题组成员表
11. `budget_transaction` - 预算交易表

#### 采购管理表
12. `procurement_request` - 采购申请表

#### 安全管理表
13. `ghs_msds` - GHS/MSDS信息表
14. `sop_document` - SOP文档表
15. `sop_training_record` - SOP培训记录表

#### 废弃物管理表
16. `waste_category` - 废弃物分类表
17. `waste_disposal_record` - 废弃物处置记录表

#### 生命周期管理表
18. `reagent_lifecycle` - 试剂生命周期表

#### 存储管理表
19. `storage_incompatibility_rules` - 存储兼容性规则表
20. `storage_location_attributes` - 存储位置属性表
21. `storage_location_coordinate` - 存储位置坐标表
22. `storage_location_map` - 存储位置地图表
23. `storage_warning_log` - 存储预警日志表

#### 预测分析表
24. `forecasting_record` - 预测记录表
25. `consumption_pattern` - 消耗模式表
26. `experiment_activity` - 实验活动表
27. `experiment_pattern` - 实验模式表

#### 辅助表
28. `scan_operation_log` - 扫描操作日志表

### 初始数据

#### 用户账号（6个）
| 用户名 | 密码 | 角色 | 姓名 |
|--------|------|------|------|
| admin | 123456 | ADMIN | 系统管理员 |
| teacher1 | 123456 | TEACHER | 张教授 |
| teacher2 | 123456 | TEACHER | 李老师 |
| student1 | 123456 | STUDENT | 王小明 |
| student2 | 123456 | STUDENT | 刘小红 |
| student3 | 123456 | STUDENT | 陈小华 |

#### 试剂分类（8个）
- 有机溶剂
- 无机酸碱
- 标准试剂
- 生物试剂
- 指示剂
- 金属盐类
- 有机化合物
- 气体

#### 试剂信息（15种）
包括常用试剂如乙醇、甲醇、丙酮、盐酸、硫酸、氢氧化钠等，每种试剂都包含：
- CAS号
- 规格
- 制造商
- 危险等级
- 详细描述

#### 存放位置（10个）
- 化学实验室A（多个柜位）
- 化学实验室B（多个柜位）
- 生物实验室
- 危险品库

#### 库存记录（15条）
每条库存记录包含：
- 批次号
- 数量
- 有效期
- 供应商
- 采购价格
- 状态（正常/库存低等）

#### 课题组（3个）
- 有机化学合成课题组（预算50万）
- 分析化学课题组（预算30万）
- 生物化学课题组（预算40万）

#### 其他数据
- 5条领用申请记录
- 5条入库记录
- 5条出库记录
- 3条采购申请记录
- 5条GHS/MSDS安全信息
- 3份SOP文档
- 5条SOP培训记录
- 4个废弃物分类
- 3条废弃物处置记录
- 7条试剂生命周期记录
- 3条存储兼容性规则
- 3条存储位置属性
- 11条预算交易记录

## 验证初始化

初始化完成后，可以通过以下SQL语句验证：

```sql
-- 使用数据库
USE reagent_management;

-- 查看所有表
SHOW TABLES;

-- 查看用户数量
SELECT COUNT(*) FROM sys_user;

-- 查看试剂数量
SELECT COUNT(*) FROM reagent;

-- 查看库存数量
SELECT COUNT(*) FROM inventory;

-- 验证管理员账号
SELECT * FROM sys_user WHERE username = 'admin';
```

## 注意事项

1. **备份提醒**：如果数据库已存在，运行此脚本会先删除原数据库再创建新的，请务必提前备份重要数据。

2. **权限要求**：执行脚本的MySQL用户需要具有创建数据库和表的权限。

3. **字符编码**：脚本使用UTF8MB4编码，支持存储emoji等特殊字符。

4. **数据安全**：初始密码均为简单密码（123456），生产环境请及时修改。

5. **表结构**：所有表都使用InnoDB引擎，支持事务处理。

## 重置数据库

如需重置数据库到初始状态，只需再次运行初始化脚本即可：

```bash
init_db.bat
```

或者手动删除数据库后重新初始化：

```sql
DROP DATABASE IF EXISTS reagent_management;
-- 然后执行初始化脚本
```

## 故障排查

### 问题1：无法连接MySQL

**解决方案**：
- 检查MySQL服务是否启动
- 确认主机地址和端口是否正确
- 验证用户名和密码

### 问题2：权限不足

**解决方案**：
```sql
-- 使用root用户授权
GRANT ALL PRIVILEGES ON reagent_management.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

### 问题3：编码问题

**解决方案**：
- 确保MySQL服务器配置支持utf8mb4
- 检查my.ini配置文件中的字符集设置

### 问题4：脚本执行失败

**解决方案**：
- 查看错误信息
- 确认SQL语法是否兼容当前MySQL版本
- 检查是否有语法错误或特殊字符

## 联系支持

如遇到问题，请检查：
1. MySQL版本（推荐5.7+或8.0+）
2. 错误日志
3. 配置文件

---

**最后更新**: 2024年11月6日



