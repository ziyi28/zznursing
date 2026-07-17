# 中州养老护理平台

基于 RuoYi-Vue 二次开发的智慧养老护理管理系统，面向养老机构的老人档案、床位房间、入住合同、护理计划、健康评估、物联网设备接入和异常预警等业务场景。项目采用前后端分离架构，后端使用 Spring Boot + Spring Security + MyBatis-Plus，前端使用 Vue2 + Element UI，并接入 Redis、Quartz、WebSocket、华为云 IoTDA、阿里云 OSS 和大模型能力。

## 项目定位

养老机构的日常运营涉及老人信息、床位资源、护理任务、健康报告、智能设备和告警通知等多类数据。本项目在 RuoYi 权限与系统管理能力的基础上，扩展了养老业务模块，并实现了设备数据采集、告警规则过滤、实时消息推送和 AI 健康评估等功能，适合作为养老院后台管理系统或智慧养老平台的后端基础。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 2.5.15、Spring Security、JWT、Quartz |
| 数据访问 | MyBatis-Plus、MyBatis XML、PageHelper、Druid |
| 数据存储 | MySQL、Redis |
| 前端框架 | Vue 2.6、Vue Router、Vuex、Element UI、Axios、ECharts |
| 物联网 | 华为云 IoTDA SDK、AMQP 1.0、Qpid JMS Client |
| 文件与 AI | 阿里云 OSS、PDF 解析、DeepSeek/OpenAI 兼容模型接口 |
| 实时通信 | WebSocket |
| 构建工具 | Maven、多模块工程、Vue CLI |

## 核心功能

### 养老业务管理

- 老人档案管理：维护老人基础信息、身份证信息、健康评估关联数据。
- 家属成员管理：记录老人家属联系方式和绑定关系。
- 楼层、房间、床位管理：支持养老机构空间资源维护和床位分配。
- 房型管理：维护房型价格、床位数量、介绍图片和状态。
- 预约管理：支持客户预约参观、预约入住等流程数据。
- 入住管理：支持入住申请、入住详情查询、老人信息和合同信息关联。
- 合同管理：支持入住合同维护，并通过定时任务处理合同相关状态。
- 护理项目、护理计划、护理等级：维护护理服务项目，组合护理计划，并关联护理等级和费用。
- 护理员负责老人：通过角色与数据权限支撑护理人员查看负责对象。

### 健康评估

- 支持上传体检 PDF 报告到 OSS。
- 解析 PDF 文本并临时缓存到 Redis。
- 调用大模型对体检报告进行结构化分析。
- 生成健康指数、风险等级、异常指标解释、系统评分和护理等级建议。
- 根据身份证自动计算年龄、性别、出生日期等基础信息。

### 物联网设备接入

- 接入华为云 IoTDA，支持同步产品列表。
- 支持设备注册、编辑、删除、详情查询。
- 支持查询设备影子数据和上报属性。
- 通过 AMQP 消费设备上报消息。
- 将设备数据批量落库，并在 Redis 中维护设备最新数据。

### 告警与实时通知

- 支持配置告警规则，包括产品、设备、功能点、阈值、比较符、持续周期、生效时间段和沉默周期。
- 支持全局规则和设备专属规则。
- 支持老人异常数据和设备异常数据两类告警。
- 根据设备绑定位置查找护理员，根据角色查找维修人员和管理员。
- 告警触发后批量保存告警数据，并通过 WebSocket 推送给指定用户。

### 系统基础能力

- 用户、角色、菜单、部门、岗位、字典、参数管理。
- RBAC 权限控制，支持菜单权限和按钮权限。
- 登录日志、操作日志、在线用户、服务监控、缓存监控。
- Quartz 定时任务管理。
- Swagger 接口文档。
- RuoYi 代码生成器。

## 系统架构

```text
zznursing
├── zzyl-admin              # Spring Boot 启动入口和后台接口聚合模块
├── zzyl-nursing-platform   # 养老业务核心模块
├── zzyl-framework          # 安全、配置、拦截器、数据源等框架能力
├── zzyl-common             # 通用工具、统一响应、异常、缓存常量、AI 调用封装
├── zzyl-system             # 用户、角色、菜单、部门等系统管理模块
├── zzyl-quartz             # 定时任务模块
├── zzyl-generator          # 代码生成模块
├── zzyl-oss                # 阿里云 OSS 文件上传模块
├── zzyl-ui                 # Vue2 + Element UI 前端工程
└── sql                     # 数据库初始化脚本
```

## 业务数据流

### 健康评估流程

```text
上传体检 PDF
  -> OSS 保存文件
  -> PDFUtil 提取报告文本
  -> Redis 按身份证缓存报告文本
  -> 新增健康评估
  -> 调用大模型生成 JSON 评估结果
  -> 保存健康评分、风险分布、异常指标和护理等级建议
```

### 设备告警流程

```text
设备上报数据到华为云 IoTDA
  -> AMQP 客户端消费消息
  -> 解析服务属性并批量写入 device_data
  -> Redis 保存设备最新数据
  -> 告警任务执行规则过滤
  -> 命中阈值、持续周期和沉默周期判断
  -> 保存 alert_data
  -> WebSocket 推送给护理员、维修人员或管理员
```

## 快速启动

### 环境要求

- JDK 11
- Maven 3.6+
- MySQL 8.x
- Redis 6.x+
- Node.js 14/16 和 npm

### 1. 初始化数据库

创建数据库：

```sql
CREATE DATABASE zzyl DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

按需导入 `sql` 目录下脚本：

```text
sql/zzyl-dev06-init.sql   # 养老平台业务和演示数据
sql/quartz.sql            # Quartz 定时任务表
sql/ry_20250417.sql       # RuoYi 基础表结构和基础数据
```

如果使用 `zzyl-dev06-init.sql` 已包含完整基础数据，可优先以该脚本为准；如果本地库缺少定时任务表，再补充执行 `quartz.sql`。

### 2. 修改后端配置

编辑：

```text
zzyl-admin/src/main/resources/application-dev.yml
```

至少需要按本地环境修改：

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://localhost:3306/zzyl?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
        username: your_mysql_user
        password: your_mysql_password
  redis:
    host: localhost
    port: 6379
    password:

aliyun:
  oss:
    endpoint: your_oss_endpoint
    bucketName: your_bucket

openai:
  deepseek:
    base-url: your_model_base_url
    api-key: your_api_key
    model: your_model

huaweicloud:
  ak: your_huawei_ak
  sk: your_huawei_sk
  endpoint: your_iotda_endpoint
  projectId: your_project_id
  accessKey: your_amqp_access_key
  accessCode: your_amqp_access_code
  queueName: your_queue_name
```

本仓库配置文件中可能包含开发环境连接项，公开到 GitHub 前建议替换为占位值或改为环境变量注入。

### 3. 启动后端

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

启动：

```bash
cd zzyl-admin
mvn spring-boot:run
```

默认后端地址：

```text
http://localhost:8080
```

Swagger 地址：

```text
http://localhost:8080/swagger-ui/index.html
```

### 4. 启动前端

```bash
cd zzyl-ui
npm install
npm run dev
```

默认前端地址：

```text
http://localhost
```

前端开发代理默认转发到：

```text
http://localhost:8080
```

### 5. 默认账号

初始化数据通常包含 RuoYi 默认管理员账号：

```text
账号：admin
密码：admin123
```

如果登录失败，请检查导入的数据脚本中 `sys_user`、`sys_role`、`sys_menu`、`sys_user_role` 是否完整。

## 主要接口模块

| 模块 | 路径前缀 | 说明 |
| --- | --- | --- |
| 老人管理 | `/nursing/elder` | 老人档案 CRUD、导出 |
| 入住管理 | `/nursing/checkIn` | 入住申请、详情、CRUD |
| 合同管理 | `/nursing/contract` | 合同 CRUD、合同状态维护 |
| 健康评估 | `/nursing/healthAssessment` | 体检报告上传、AI 评估、评估记录 |
| 护理项目 | `/nursing/project` | 护理服务项目维护 |
| 护理计划 | `/nursing/plan` | 护理计划维护 |
| 护理等级 | `/nursing/level` | 护理等级和费用维护 |
| 楼层房间床位 | `/nursing/floor`、`/nursing/room`、`/nursing/bed` | 空间资源管理 |
| 设备管理 | `/nursing/device` | IoT 产品同步、设备注册、设备详情、设备影子 |
| 设备数据 | `/nursing/deviceData` | 设备上报数据分页查询 |
| 告警规则 | `/nursing/alertRule` | 告警规则 CRUD |
| 告警数据 | `/nursing/alertData` | 告警记录查询和处理 |
| WebSocket | `/ws/{userId}` | 指定用户实时告警推送 |

## 项目亮点

- 在 RuoYi 权限体系上扩展养老院真实业务模型，覆盖从预约、入住、合同到护理等级的完整链路。
- 接入华为云 IoTDA，实现产品同步、设备注册、设备影子查询和 AMQP 数据消费。
- 设计基于 Redis 的设备最新数据缓存与告警持续周期计数，减少数据库扫描压力。
- 告警规则支持生效时段、沉默周期、全局/设备专属规则和角色定向通知。
- 使用 WebSocket 将告警消息实时推送给护理员、维修人员和管理员。
- 健康评估模块结合 PDF 解析、OSS 存储、Redis 缓存和大模型结构化分析，自动生成风险等级与护理建议。
- 保留 RuoYi 的用户权限、日志、监控、定时任务和代码生成能力，便于后台管理系统快速扩展。

## 打包部署

后端打包：

```bash
mvn clean package -DskipTests
```

生成的可运行 Jar 位于：

```text
zzyl-admin/target/zzyl-admin.jar
```

运行：

```bash
java -jar zzyl-admin/target/zzyl-admin.jar --spring.profiles.active=prod
```

前端打包：

```bash
cd zzyl-ui
npm run build:prod
```

构建产物位于：

```text
zzyl-ui/dist
```

## 注意事项

- 首次运行前必须配置 MySQL 和 Redis。
- IoT、OSS、AI 健康评估属于外部服务能力，没有对应云服务配置时，相关功能会不可用，但基础后台管理功能仍可运行。
- 公开仓库前建议移除 `target/`、`.idea/` 等本地构建和 IDE 文件。
- 生产环境不要将数据库密码、云服务 AK/SK、模型 API Key 直接提交到仓库，建议使用环境变量、配置中心或密钥管理服务。

## License

本项目基于 RuoYi-Vue 二次开发，遵循仓库中的 `LICENSE`。
