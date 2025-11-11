# 从 gRPC 推送迁移到 Nacos 风格轮询通信 TODO

> 目标：用 HTTP/REST + 30s（可配置）轮询替代现有 gRPC 推送，实现类似 Nacos 2.x 的通信机制，同时兼顾所有模块（core/link/server/client/spring-boot/embedded/example）。

## TODO 列表

1. **协议与核心 DTO（core/link）** ✅
   - 重新定义轮询请求/响应模型，包含租户、命名空间、appId、labels、版本/etag 等信息。
   - 新增轮询策略抽象（如 `PollRequest`, `PollResponse`, `PollAdvice`），支持“下次轮询间隔”字段。
   - 处理 config 变更事件模型的调整，从推模式改为“基于版本的对比”。

2. **客户端 SDK（lighting-config-client）** ✅
   - 废弃 gRPC `ConfigTransport`，实现新的 HTTP 轮询传输层，负责周期性获取配置差异。
   - 在 `ClientOptions` 中引入 `pollInterval`（默认 30s，可配置，单位毫秒），支持 jitter/backoff。
   - 重写 `LightingClient.start()`：以调度线程执行轮询；合并变更后触发监听。
   - 更新缓存与版本管理逻辑，使其基于“最后一次版本号/etag”。
   - 调整日志与 banner，反映轮询通信；确保 close() 终止调度器。

3. **Spring Boot Starter（lighting-config-spring-boot）** ✅
   - 绑定 `lighting.config.client.poll-interval` 配置项，默认 30s。
   - 自动装配新的轮询 transport，无需 gRPC 依赖。
   - 更新测试，验证 poll interval 配置与 Transport 注入逻辑。

4. **服务器模块（lighting-config-server）** (进行中)
   - ✅ 增加 `/api/poll` REST 端点，返回 `PollResponse`（当前基于快照 + 版本过滤）。
   - ✅ 移除所有 gRPC 组件与依赖（服务端实现、配置项、根 POM 中的 gRPC BOM 及 protoc 插件已删除）。
   - 结合 `ConfigApplicationService`/`NotifyEngine` 计算差异列表与版本；必要时新增内存缓存或长轮询机制。
   - 提供可选的限流/安全策略（轻量 token），以支撑高频轮询。

5. **Embedded 模式（lighting-config-embedded）**
   - 替换 `EmbeddedConfigTransport` 的实现，使其重用新的轮询服务（在同 JVM 内可直接调用 service）。
   - 确保 embedded 场景仍能配置更短的 poll interval，并保留 banner/监听功能。

6. **示例与文档**
   - 更新 `lighting-config-example` 子模块配置与说明，展示 `lighting.config.client.poll-interval` 的使用。
   - 在 `docs/系统设计文档.md`、`docs/example.md` 等文件中描述新的轮询通信流程与时序图。
   - 为 README/Runbook/ADR 添加迁移说明。

7. **依赖与清理** ✅
   - 已从所有 POM 中移除 gRPC 相关依赖、`lighting-config-link` 模块及 proto 目录。

8. **测试与验证**
   - 编写针对轮询请求/响应的单元与集成测试。
   - 在示例客户端中演示不同轮询间隔与手动刷新。
   - 制定性能/压力测试策略，验证轮询负载。

> 进度：已完成任务 1（核心 DTO）与任务 2（客户端切换到轮询：新增 `PollingTransport`、HTTP 传输、`LightingClient` 定时轮询架构、Spring Boot Starter 与示例更新）。其余任务按序推进。

> 执行策略：按顺序逐步完成上述事项，每完成一个阶段更新该文档并回填实现细节。
