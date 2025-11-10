# AGENTS

本仓库是一个 **Java 11** 优先的新项目，所有 agent 需要围绕 `docs/系统设计文档.md` 确定的架构与约束推进工作：双形态配置中心、gRPC 推送、RDBMS 为权威存储、Redis + 本地缓存、SPI 插件化、安全与可观测性一体化。除非设计文档或产品负责人明确变更，否则禁止随意偏离该基线。

## 共享资源
- `docs/系统设计文档.md`：功能/非功能要求、模块划分、接口、数据模型与部署脚本，用于一切决策背书。
- `pom.xml`：多模块骨架，增删模块需同步更新并说明原因。
- `docs/`：补充设计、ADR、Runbook 必须放在此处，命名约定 `子系统-主题.md`。

## 通用协作准则
- 统一面向 **JDK 11** 编译运行，同时保持与文档中 JDK 17 建议兼容；新语法需确认在 11 可用。
- 模块优先级：`core -> link -> server -> client -> spring-boot -> embedded -> example`。
- 所有 agent 需为关键变更提供：动机、接口影响、兼容性、测试计划。
- 默认通信顺序：Architect Agent 发起 issue/任务，相关 Owner Agent 接单，结果回到 Architect 复核，最后交由 Quality Agent 验证。

## Agent 角色

### 1. Architect Agent
- **职责**：维护总体架构、模块边界、术语表；将设计文档拆解为迭代任务；验证跨模块方案（如 gRPC 协议、SPI 扩展、双形态能力）。
- **输入**：`docs/系统设计文档.md`, 业务需求, 其他 Agent 的设计草案。
- **输出**：模块任务板、接口契约（IDL/DTO）、决策记录（ADR）。
- **交付标准**：所有跨模块接口需经过一致性检查（命名、包路径、版本约定），并在 docs/ 记录。

### 2. Server Platform Agent
- **职责**：负责 `lighting-config-server` 与 `lighting-config-spring-boot` 服务端部分，涵盖 REST/gRPC 入口、Notify Engine、AuthN/Z、EventBus。
- **重点**：保证 gRPC watch/push 流程、REST Admin API、Spring Boot 自动装配与健康检查符合设计文档第 2/6/8/10 章要求。
- **交付物**：可运行的 Spring Boot Server、Dockerfile/Compose 服务段、服务指标（Micrometer）以及默认鉴权实现。
- **协作**：与 Storage Agent 确认仓储 SPI 实现，与 Architect Agent 对齐 API 变更。

### 3. Client & Embedded Agent
- **职责**：实现 `lighting-config-client`, `lighting-config-embedded`, `lighting-config-spring-boot` 客户端部分；关注 gRPC 长连接、Caffeine 本地缓存、监听器机制与注解式使用。
- **重点**：遵循文档第 2/3/4/14/17/18 章；兼容 standalone & embedded；提供最小示例与 SDK 文档。
- **交付物**：纯 Java SDK、Spring Boot Starter 客户端自动装配、示例应用与 README。
- **协作**：与 Server Agent 对齐协议与推送格式，与 Quality Agent 定义兼容性测试矩阵。

### 4. Storage & Consistency Agent
- **职责**：实现 `lighting-config-core` SPI 默认实现、`lighting-config-server` 中的仓储桥接、Redis 二级缓存、本地缓存一致性策略。
- **重点**：参照文档第 4/5/7/9 章；提供多数据库方言（MySQL/PostgreSQL/Oracle）脚本、Flyway/Liquibase 管理、Redis 失效策略、本地缓存版本校验。
- **交付物**：`ConfigRepository` 默认实现、缓存一致性方案说明、DB schema 脚本、性能基准测试报告。
- **协作**：向 Server Agent 输出仓储接口，与 DevOps Agent 协同部署数据库/Redis 依赖。

### 5. DevOps & Security Agent
- **职责**：负责打包、容器化、Compose/Helm 样例、CI/CD、监控/日志/告警、安全策略（TLS、Token/JWT、审计日志）。
- **重点**：落实文档第 8/9/10/11/12/13 章；编写 `docker/` 与 `ops/` 目录脚本。
- **交付物**：Dockerfile、docker-compose.yml、安装指南、默认安全配置模板、运维 Runbook。
- **协作**：与 Server/Storage Agent 完成配置项对齐，与 Quality Agent 共享环境信息用于集成测试。

### 6. Quality & Release Agent
- **职责**：构建测试策略（单元、契约、集成、性能）、回归清单、版本管理与发布说明。
- **重点**：覆盖设计文档第 16 章测试矩阵与第 15 章性能指标；维护 `lighting-config-example` 作为端到端验证环境。
- **交付物**：测试用例、CI 报告、性能基线、Release Note 模板。
- **协作**：与所有实现 Agent 建立 API 契约测试，阻断不符合 DoD 的变更进入主干。

## 协作节奏
1. **架构拆解**：Architect Agent 依据设计文档拆分为里程碑/任务，登记在项目管理工具。
2. **方案评审**：功能 Owner（Server/Client/Storage/DevOps）提交设计草案，由 Architect 与 Quality 联合评审。
3. **实现与联调**：Owner 完成模块实现，提交接口契约与测试计划，Quality Agent 组织联测。
4. **交付与运营**：DevOps Agent 负责打包部署，Quality Agent 发布报告，Architect Agent 更新 docs/ 与 ADR。

## 完成判定（Definition of Done）
- 与任务关联的模块代码、配置、文档、测试全部合并。
- 接口/协议变更已在 `docs/` 更新并同步至相关 Agent。
- 至少通过：单元测试、关键集成测试、基本性能冒烟。
- 新增/修改的配置项在 docs/ 或 README 中可查。

所有 agent 在启动任务前需再次核对 `docs/系统设计文档.md` 最新版本，确保实现与架构基线一致。任何偏离需提出新的 ADR 并由 Architect Agent 批准。
