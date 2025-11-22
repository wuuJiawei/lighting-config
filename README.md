# lighting-config

Lightweight configuration center for Java services. 支持独立部署与嵌入式两种形态，基于 HTTP 长轮询 + 本地缓存，默认以 RDBMS 为权威存储（Caffeine 做热点缓存），提供 SPI 插件化、安全与可观测能力。

## 特性
- 双形态：Standalone Server（`lighting-config-server`）或业务内嵌（`lighting-config-embedded`）。
- 高效下发：HTTP 长轮询 + 版本号校验，本地 Caffeine 缓存毫秒级刷新。
- 可靠存储：默认 JDBC（MySQL/PostgreSQL/Oracle），提供多方言脚本与缓存失效策略。
- 插件化：核心协议/模型在 `lighting-config-core`，存储、鉴权、灰度策略均可通过 SPI 扩展。
- 安全/观测：Token/JWT、命名空间/租户隔离、指标 & 健康检查，便于上云与审计。
- 生态：纯 Java SDK、Spring Boot 2.x/3.x Starter、示例工程与未来的 Web 控制台。

## 模块一览
- `lighting-config-core`：领域模型、协议、SPI（无 Spring 依赖）。
- `lighting-config-server`：默认服务端实现（Spring Boot），内置 HTTP Poll + Admin API。
- `lighting-config-client`：通用客户端 SDK，含长轮询、本地缓存、监听器。
- `lighting-config-spring-boot-starter` / `lighting-config-spring-boot3-starter`：Spring Boot 2.x/3.x 自动装配。
- `lighting-config-embedded`：嵌入式模式，业务进程内直接托管配置。
- `lighting-config-example/*`：示例应用（embedded / standalone server / client）。
- `lighting-config-console`：Web 控制台，方案见 `docs/frontend-architecture.md`。

## 快速开始
前置：JDK 11+（兼容 17）、Maven 3.8+，如需独立服务端请准备 MySQL/PG/Oracle 数据源。

### 1）快速体验示例
- Embedded：`cd lighting-config-example/lighting-config-example-embedded && mvn spring-boot:run`
- 轻量 Server 示例：`cd lighting-config-example/lighting-config-example-standalone && mvn spring-boot:run`
- Spring Boot 客户端示例：`cd lighting-config-example/lighting-config-example-client && mvn spring-boot:run`

### 2）启动正式服务端
准备 `application.yml`（示例使用 PostgreSQL，可替换为 MySQL/Oracle）：

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
        token: ${LIGHTING_CONFIG_TOKEN}
```

启动：

```bash
mvn -pl lighting-config-server -am spring-boot:run
```

数据库 DDL 见 `docs/schema/`，更多配置项见 `docs/server-configuration.md`。

### 3）客户端接入（Spring Boot）
`pom.xml` 引入 Starter（2.x 环境）：

```xml
<dependency>
  <groupId>io.lighting</groupId>
  <artifactId>lighting-config-spring-boot-starter</artifactId>
  <version>${lighting.config.version}</version>
</dependency>
```

配置服务端地址与命名空间：

```yaml
lighting:
  config:
    client:
      server:
        address: http://localhost:7086
      namespace: default
      app-id: demo-client
      poll-interval: 30s
```

在业务代码中使用注解即可自动拉取并热刷新：

```java
@RestController
class FeatureController {
  @LightingValue(key = "feature.order.v2", defaultValue = "false")
  private boolean orderV2Enabled;

  @GetMapping("/feature/order")
  public boolean orderV2() {
    return orderV2Enabled;
  }
}
```

非 Spring 场景可直接使用 `lighting-config-client` 提供的 `LightingClient`/监听器接口，详见 `docs/client-configuration.md`。

## 架构与文档
- 设计基线：`docs/系统设计文档.md`
- 控制台方案：`docs/frontend-architecture.md`
- 客户端配置：`docs/client-configuration.md`
- 服务端配置：`docs/server-configuration.md`
- 示例说明：`docs/example.md`

## 构建与测试
- 全量构建：`mvn clean verify`
- 仅编译（跳过测试）：`mvn -DskipTests install`
- Spring Boot 3 相关模块：`mvn -P spring-boot3 -pl lighting-config-spring-boot3-starter -am verify`

## 发布到 Maven Central（摘要）
1. 选定开源协议（建议 Apache-2.0），在仓库根目录补充 `LICENSE`、`NOTICE`、`CODE_OF_CONDUCT.md`、`CONTRIBUTING.md`。
2. 为所有子模块补齐 POM 元数据：`<name>`、`<description>`、`<url>`、`<licenses>`、`<developers>`、`<scm>`，并设置 `distributionManagement` 指向 `s01.oss.sonatype.org`。
3. 添加发布 profile：`maven-source-plugin`、`maven-javadoc-plugin`、`maven-gpg-plugin`（签名）、`nexus-staging-maven-plugin`，使用 `mvn -P release deploy` 生成 staging。
4. 在 `~/.m2/settings.xml` 配置 Sonatype 账户与 GPG 密钥（`gpg --full-generate-key`），保持版本号非 SNAPSHOT。
5. 通过 Sonatype UI 或 `nexus-staging:release` 关闭并发布 staging 仓库，等待同步到 Maven Central 后打 tag 并 bump 下一版本。

更多发布细节可在后续补充到 `docs/` 或 CI 脚本中。

## 许可证
Apache License 2.0，见 `LICENSE`。
