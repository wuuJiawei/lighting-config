# lighting-config

[English](README.en.md) | [简体中文](README.md)

[![Maven Central](https://img.shields.io/maven-central/v/pub.lighting/lighting-config-spring-boot-starter.svg)](https://central.sonatype.com/artifact/pub.lighting/lighting-config-spring-boot-starter)
![JDK](https://img.shields.io/badge/JDK-17%2B-0b7cfa)

Lightweight configuration center for Java services. 支持独立部署与嵌入式两种形态，基于 HTTP 长轮询 + 本地缓存，默认以 RDBMS 为权威存储（Caffeine 做热点缓存），提供 SPI 插件化、安全与可观测能力。

## 特性
- 双形态：Standalone Server（`lighting-config-server`）或业务内嵌（`lighting-config-embedded`）。
- 高效下发：HTTP 长轮询 + 版本号校验，本地 Caffeine 缓存毫秒级刷新。
- 可靠存储：默认 JDBC（MySQL/PostgreSQL/Oracle），提供多方言脚本与缓存失效策略。
- 插件化：核心协议/模型在 `lighting-config-core`，存储、鉴权、灰度策略均可通过 SPI 扩展。
- 安全/观测：Token/JWT、命名空间/租户隔离、指标 & 健康检查，便于上云与审计。
- 生态：纯 Java SDK、Spring Boot 3 Starter、示例工程与未来的 Web 控制台。

## 模块一览
- `lighting-config-core`：领域模型、协议、SPI（无 Spring 依赖）。
- `lighting-config-server`：默认服务端实现（Spring Boot），内置 HTTP Poll + Admin API。
- `lighting-config-client`：通用客户端 SDK，含长轮询、本地缓存、监听器。
- `lighting-config-spring-boot-starter`：Spring Boot 3 自动装配。
- `lighting-config-embedded`：嵌入式模式，业务进程内直接托管配置。
- `lighting-config-example/*`：示例应用（embedded / standalone server / client）。
- `lighting-config-console`：Web 控制台，方案见 `docs/frontend-architecture.md`。

## 快速入门
前置：JDK 17、Maven 3.8+，如需独立服务端请准备 MySQL/PG/Oracle 数据源。最新可用版本见上方 Maven Central badge（示例使用 `1.0.1`）。

### 1）启动服务端并验证
1. 准备 `application.yml`（示例使用 PostgreSQL，可替换为 MySQL/Oracle）：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lighting_config
    username: lighting
    password: lighting
lighting:
  config:
    mode: standalone
    server:
      port: 7086
    storage:
      type: jdbc
    auth:
      enabled: true
      mode: token
      options:
        tokens: ${LIGHTING_CONSOLE_TOKENS:lighting-console-token}
```

2. 启动并打开接口文档：

```bash
mvn -pl lighting-config-server -am spring-boot:run
open http://localhost:7086/lighting-config/api/docs
```

默认 token 为 `lighting-console-token`（可通过 `X-Lighting-Token` 或 `Authorization: Bearer <token>` 传递）。数据库 DDL 见 `docs/schema/`，更多配置项见 `docs/server-configuration.md`。

3. 通过 Admin API 写入一条配置（例：`feature.order.v2=true`），客户端即可轮询到：

```bash
 curl -XPOST http://localhost:7086/lighting-config/api/admin/config \
  -H 'Content-Type: application/json' \
  -H 'X-Lighting-Token: lighting-console-token' \
  -d '{"tenant":"default","namespace":"default","appId":"demo-client","key":"feature.order.v2","value":"true","contentType":"STRING"}'
```

4. 控制台入口（Standalone 或嵌入式均可）：浏览器访问 `http://<host>:<port>/lighting-config/index.html`，示例为 `http://localhost:7086/lighting-config/index.html`，登录时填入同一 token。

### 2）业务接入速览
#### Spring Boot Starter（推荐）
`pom.xml` 引入 Starter（最新版本见 badge，示例使用 `1.0.1`）：

```xml
<dependency>
  <groupId>pub.lighting</groupId>
  <artifactId>lighting-config-spring-boot-starter</artifactId>
  <version>1.0.1</version>
</dependency>
```

配置服务端地址与作用域：

```yaml
lighting:
  config:
    client:
      server:
        address: http://localhost:7086
      tenant: default
      namespace: default
      app-id: demo-client
      poll-interval: 15s
```

在业务代码中使用两个核心注解：`@LightingValue` 拉取/热刷值，`@LightingListener` 监听变更。

```java
@RestController
class FeatureController {
  @LightingValue(key = "feature.order.v2", defaultValue = "false")
  private boolean orderV2Enabled;

  @LightingListener(prefix = "feature.")
  public void onFeatureChanged(ConfigChange change) {
    log.info("feature changed: {} -> {}", change.getCoordinate().getKey(), change.getValue());
  }

  @GetMapping("/feature/order")
  public boolean orderV2() {
    return orderV2Enabled;
  }
}
```

复杂对象可用 `@LightingProperties` 一次性注入并随配置中心热刷新：

```java
@Component
@LightingProperties(prefix = "order.routing")
class OrderRoutingProperties {
  private boolean enabled = true;
  private Duration slowThreshold = Duration.ofSeconds(2);
  private int workerPoolSize = 4;
  private List<String> preferredRegions = List.of("ap-shanghai", "ap-singapore");
  private Map<String, Integer> regionWeights = Map.of("ap-shanghai", 70, "ap-singapore", 30);

  // getter 省略；修改配置即可实时更新本 Bean
}
```

#### 纯 Java SDK（非 Spring 场景）
`pom.xml` 引入客户端 SDK：

```xml
<dependency>
  <groupId>pub.lighting</groupId>
  <artifactId>lighting-config-client</artifactId>
  <version>1.0.1</version>
</dependency>
```

非 Spring 场景使用 `LightingClient` + `HttpPollingTransport` 即可：

```java
ClientOptions options = ClientOptions.builder()
    .serverAddress("http://localhost:7086")
    .tenant("default")
    .namespace("default")
    .appId("demo-client")
    .pollInterval(Duration.ofSeconds(10))
    .authToken("lighting-console-token")
    .build();

LightingClient client = new LightingClient(options, new HttpPollingTransport(options));
client.addListener("feature.", change -> System.out.println("updated: " + change.getValue()));
client.start(); // 非守护线程，请在 JVM 退出前调用 close

String feature = client.get("feature.order.v2").orElse("false");
System.out.println("flag=" + feature);
```

#### 嵌入式模式（无远端服务端）
`pom.xml` 引入嵌入式包：

```xml
<dependency>
  <groupId>pub.lighting</groupId>
  <artifactId>lighting-config-embedded</artifactId>
  <version>1.0.1</version>
</dependency>
```

使用 `EmbeddedConfigManager` 在进程内托管配置，适合单机/边缘场景，避免外部依赖。参考模块 `lighting-config-example-embedded` 的配置与启动方式。

**接入注意事项**
- Key 写法宽容：服务端存储用点分隔（如 `order.routing.worker-pool-size`），客户端读取时支持驼峰 / 下划线 / 中划线互通，`workerPoolSize`、`worker-pool-size`、`worker_pool_size` 均可匹配同一配置键。
- 格式与解码：`contentType` 支持 STRING/BOOLEAN/INT/LONG/FLOAT/DOUBLE/LIST/MAP，Starter 会按类型自动转换并支持 JSON 绑定 POJO（见 `@LightingProperties` 示例）。
- 作用域合并：`lighting.config.client.app-id` 可逗号分隔多个值，客户端自动在末尾追加内置 `__global__`，并按顺序优先级合并（业务作用域覆盖全局）。

### 3）快速体验示例
- Embedded：`cd lighting-config-example/lighting-config-example-embedded && mvn spring-boot:run`
- 轻量 Server 示例：`cd lighting-config-example/lighting-config-example-standalone && mvn spring-boot:run`
- Spring Boot 客户端示例：`cd lighting-config-example/lighting-config-example-client && mvn spring-boot:run`

## Docker 部署
- 一键启动（含 PostgreSQL）：`cp docker/.env.example docker/.env && docker compose -f docker/docker-compose.yml up -d`
- 外部数据库：在 `.env` 中设置 `LIGHTING_DB_URL/LIGHTING_DB_USER/LIGHTING_DB_PASSWORD`，运行 `docker compose -f docker/docker-compose.external-db.yml up -d`
- 详细说明见 `docs/devops-docker-deployment.md`

## 架构与文档
- 设计基线：`docs/系统设计文档.md`
- 控制台方案：`docs/frontend-architecture.md`
- 客户端配置：`docs/client-configuration.md`
- 服务端配置：`docs/server-configuration.md`
- 示例说明：`docs/example.md`

## 构建与测试
- 全量构建：`mvn clean verify`
- 仅编译（跳过测试）：`mvn -DskipTests install`
- 基线环境：JDK 17 + Spring Boot 3.2.x（无需额外 profile）

## 许可证
Apache License 2.0，见 `LICENSE`。
