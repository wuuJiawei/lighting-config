# lighting-config 示例说明

`lighting-config-example` 现拆分为多个子模块，用来演示不同场景：

| 子模块 | 说明 |
| --- | --- |
| `lighting-config-example-embedded` | 完全本地嵌入式示例，直接读取/写入 `example-config.json` |
| `lighting-config-example-standalone` | 轻量 REST 服务端示例（`/example/config` CRUD） |
| `lighting-config-example-client` | Spring Boot 客户端示例，演示 `@LightingValue`/`@LightingListener` |

## Embedded Demo（内嵌配置中心）

```bash
cd lighting-config-example/lighting-config-example-embedded
mvn spring-boot:run
```

启动后（默认端口 8083）：

- `POST /embedded/config` 新增/修改配置（JSON: tenant/namespace/appId/key/value/contentType）
- `GET /embedded/config?tenant=default&namespace=default&appId=demo-client` 查看当前值
- `DELETE /embedded/config?...` 删除配置
- 数据会持久化到项目根目录的 `example-config.json`

该 Demo 直接依赖 `EmbeddedConfigManager`，默认配置 `lighting.config.mode=embedded` 且跳过 gRPC。

## Standalone Server（轻量示例服务端）

```bash
cd lighting-config-example/lighting-config-example-standalone
mvn spring-boot:run
```

默认端口 8081，暴露 `/example/config` REST 接口并使用 `InMemoryConfigApplicationService` 保存数据：

- `GET /example/config?tenant=default&namespace=default&appId=demo-server&prefix=feature.` 查询（支持 key/prefix）
- `POST /example/config` 写入：`{"key":"feature.order.v2","value":"true"}`
- `DELETE /example/config?...&key=feature.order.v2` 删除配置

模块内部显式设置 `lighting.config.client.enabled=false`，避免启动任何 gRPC 客户端。

## Spring Boot 客户端

```bash
cd lighting-config-example/lighting-config-example-client
mvn spring-boot:run
```

默认端口 8082，对外暴露：

- `GET /feature/order`：查看 `feature.order.v2` 参数
- `GET /events/last`：查看最近一次监听事件

将服务器（无论是 `lighting-config-server` 还是示例 standalone）地址写入 `lighting.config.client.server.address` 即可完成联调，示例配置默认指向 `dns:///localhost:9091`。
