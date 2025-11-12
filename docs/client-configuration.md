# Client 端配置说明 (`lighting.config.client.*`)

`LightingClientProperties` 定义了 Spring Boot Starter 的全部配置项（`LightingClientAutoConfiguration` 会读取它并初始化 `LightingClient`）。本文列出各项含义、默认值与推荐写法，帮助业务快速接入。

## 1. 快速示例

```yaml
lighting:
  config:
    client:
      enabled: true
      auto-start: true
      tenant: default
      namespace: prod
      app-id: order-service
      labels:
        region: apac
        zone: gz-a
      metadata:
        clientId: ${HOSTNAME}
      bootstrap-prefixes:
        - datasource.
        - feature.
      poll-interval: 30s
      banner-enabled: false
      server:
        address: http://lighting-config-server:7086/lighting-config
        tls: false
```

> **全局作用域说明**：`app-id` 支持逗号分隔（例如 `order-service,android`）。无论是否显式写 `__global__`，客户端都会在请求时自动追加该内置作用域，用于加载公共配置。

## 2. 属性一览

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.client.enabled` | `boolean`，`true` | 关闭后整个 starter 不会初始化 `LightingClient`。 |
| `lighting.config.client.auto-start` | `boolean`，`true` | 若为 `false`，需要手动调用 `LightingClient.start()`。 |
| `lighting.config.client.tenant` | `String`，`default` | 租户标识，对应 `config_item.tenant`。 |
| `lighting.config.client.namespace` | `String`，`default` | 命名空间标识（例如 `prod`、`staging`）。 |
| `lighting.config.client.app-id` | `String`，`default` | 应用作用域；支持逗号分隔写多个值（按顺序匹配，最终仍会自动追加 `__global__`）。 |
| `lighting.config.client.labels` | `Map<String,String>`，默认空 | 客户端侧标签，写入 `PollRequest.labels`，用于服务端自定义筛选或审计（可选）。 |
| `lighting.config.client.metadata` | `Map<String,String>`，默认空 | 自定义元数据，与 labels 类似但语义模糊（如 `clientId`、`region`). |
| `lighting.config.client.bootstrap-prefixes` | `List<String>` ，默认空 | 客户端第一次启动（`lastVersion=0`）时只拉取指定前缀，减少快照体积。留空表示全量。 |
| `lighting.config.client.poll-interval` | `Duration`，`30s` | 定期轮询的基础间隔，单位使用 Spring Boot `Duration` 语法。 |
| `lighting.config.client.banner-enabled` | `boolean`，`true` | 控制启动 ASCII banner。 |

### server.* 子属性

| 属性 | 类型 / 默认值 | 说明 |
| --- | --- | --- |
| `lighting.config.client.server.address` | `String`，`http://localhost:7086` | HTTP 轮询的基础地址。建议带上 server 的 context-path（默认 `/lighting-config`），例如 `http://10.0.0.10:7086/lighting-config`。 |
| `lighting.config.client.server.tls` | `boolean`，`false` | 指示是否启用 TLS（目前仅作为 metadata，用于后续 gRPC/HTTP2）。若使用 HTTPS，请直接把 `address` 设为 `https://...`。 |

## 3. 运行时行为提示

1. **多作用域合并**：客户端会解析 `app-id` 字符串（逗号分隔），再自动追加 `__global__`。应用作用域靠前优先级更高，`__global__` 总在最后，只补充缺失键。
2. **标签/元数据**：目前服务端默认只持久化，不做硬性约束；未来可以在 REST API 查询时通过 `labels/env` 等条件过滤。
3. **开关逻辑**：`enabled=false` 会彻底跳过 AutoConfiguration；`auto-start=false` 只是不自动启动轮训线程。
4. **Address 规范**：`HttpPollingTransport` 会把地址标准化（去掉末尾 `/`），并自动拼接 `/api/poll`。若后端部署在 `/lighting-config` context 下，请把 `address` 设置为 `http://host:port/lighting-config`。

## 4. 与 Spring 管理的 Bean 对应关系

- `LightingClientProperties` → `@ConfigurationProperties`，由 Spring Boot 注入。
- `LightingClient` → 需要的参数全部来自上述属性，若要在代码里覆盖，可注入 `LightingClient` 并调用 `client.toBuilder()` 生成新的 `ClientOptions`。
- 所有注解（`@LightingValue`, `@LightingListener`）依赖 `LightingClient` 是否成功启动；若 `enabled=false`，这些注解将不起作用。
