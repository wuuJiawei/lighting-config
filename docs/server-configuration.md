# Server 端配置说明 (`lighting.config.*`)

`LightingServerProperties`（在 `LightingConfigServerApplication` 中启用）负责承载所有配置中心服务端的扩展项。下文列出常用属性、默认值和典型用途，并附带示例。

## 1. 快速示例

```yaml
lighting:
  config:
    mode: standalone                # standalone / embedded
    server:
      port: 7086
      graceful-shutdown: 10s
    storage:
      type: jdbc
      settings:
        schema: lighting_config     # 可选，自定义 schema 或表前缀
    auth:
      enabled: true
      mode: token
      options:
        token: ${LIGHTING_CONFIG_TOKEN}

# JDBC 数据源（storage.type=jdbc 时必备）
spring:
  datasource:
    url: jdbc:postgresql://pg:5432/lighting_config
    username: lighting
    password: lighting
```

## 2. 顶层属性

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.mode` | `STANDALONE` | - `STANDALONE`：独立部署，通过 HTTP/gRPC 对外提供服务。<br>- `EMBEDDED`：与业务应用在同 JVM（如 `lighting-config-embedded` 模式）。 |
| `lighting.config.server` | 见下节 | 控制 server 监听端口与优雅停机。 |
| `lighting.config.storage` | 见下节 | 数据持久化方案（默认 JDBC）。 |
| `lighting.config.auth` | 见下节 | 鉴权配置（默认启用简单 token 模式）。 |

## 3. `server.*`

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.server.port` | `int`，`8080` | Spring Boot Web 服务器端口。通常与 `server.port` 保持一致，如已配置 `server.port` 可以省略。 |
| `lighting.config.server.graceful-shutdown` | `Duration`，`10s` | 服务停止时等待未完成请求的最长时间。 |

> 如果项目已经通过 `server.port`、`server.shutdown.grace-period` 管控，可保持 `lighting.config.server.*` 默认值。

## 4. `storage.*`

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.storage.type` | `String`，`jdbc` | 持久化类型。当前支持 `jdbc`（默认）与实验性 `memory`。 |
| `lighting.config.storage.settings` | `Map<String,String>` | 类型自定义配置。对于 `jdbc` 可用键：`schema`（自定义 schema）、`table-prefix` 等，未来用于 SPI 扩展。 |

> 当 `type=jdbc` 时，需要提供标准的 `spring.datasource.*` 属性或自定义 `DataSource` Bean。Flyway/DDL 见 `docs/schema/`。

## 5. 本地缓存（Caffeine）

lighting-config-server 内置基于 Caffeine 的读缓存，对 `config_item` 的单点读取与前缀列表做缓存，典型策略如下：

- 最近 50k 个单键读取缓存 5 分钟；
- 最近 5k 个 `(tenant, namespace, appId, prefix)` 查询缓存 2 分钟；
- `upsert`/`delete` 会立即失效对应条目及相关前缀，确保一致性；
- 若缓存 miss，则回退到数据库。

该机制完全内置，无需额外配置或 Redis 依赖，可在内存受控的同时降低数据库压力。

## 6. `auth.*`

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.auth.enabled` | `boolean`，`true` | 全局开关。 |
| `lighting.config.auth.mode` | `String`，`token` | 认证模式。当前支持 `token`（静态 Token 验证），后续可扩展 `jwt`、`oauth`。 |
| `lighting.config.auth.options` | `Map<String,String>` | 模式相关参数。例如 `mode=token` 时可配置 `token=<value>`，多个 token 可写成 `tokens=tokenA,tokenB`（需在 `AuthService` 中解析）。 |

如果暂时不需要鉴权，可将 `enabled` 设为 `false`，但生产环境建议至少开启 token 校验。

## 7. 其他说明

1. **DataSource 管理**：`lighting-config-server` 直接复用 Spring 的 `DataSource`。可通过 `spring.datasource.*`、连接池 Bean 或外部配置中心提供。
2. **模式切换**：`mode=EMBEDDED` 时，Server 会跳过内置 HTTP 端口，只保留 service/notify 组件，适配嵌入式 SDK。
3. **缓存一致性**：由于缓存位于本地 JVM，每次写入都会失效相关键。若运行多实例，依旧依赖变更事件（ChangeFeed + 客户端长轮询）拉取最新值。
4. **与例子工程配套**：`lighting-config-example-standalone` 中提供了最小化的 `application.yml`，可参照修改上述属性。
