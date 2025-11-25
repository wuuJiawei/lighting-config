# DevOps - Docker 部署指南

> 对齐 `docs/系统设计文档.md` 第 11 章，并参考 Nacos 3 的开源 docker-compose 方案，提供两种形态：
> 1）附带数据库的一键启动；2）对接外部数据库的精简部署。

## 镜像构建

- 目录：`docker/Dockerfile`，多阶段构建（`maven:3.9.6-eclipse-temurin-17` → `eclipse-temurin:17-jre`），默认打包 `lighting-config-server` 并内嵌控制台静态资源。
- 直接构建：
  ```bash
  docker build -t lighting-config-server:latest -f docker/Dockerfile .
  ```
  `JAVA_OPTS` 及 `LIGHTING_*` 环境变量在运行时可覆盖。

## 一键启动（内置 PostgreSQL）

- 模板：`docker/docker-compose.yml`，等价于 Nacos 的 standalone 场景，附带 PG 容器并自动构建 Server 镜像。
- 快速启动：
  ```bash
  cp docker/.env.example docker/.env   # 可按需修改端口/token/数据库名
  docker compose -f docker/docker-compose.yml up -d
  ```
- 端口：默认 `7086` 对外暴露；PostgreSQL 暴露 `5432`（可通过 `.env` 覆盖）。
- 数据持久化：`lighting-config-db-data` 本地卷。
- 关键环境变量（映射到 `application.yml`）：
  - `LIGHTING_DB_URL` / `LIGHTING_DB_USER` / `LIGHTING_DB_PASSWORD` / `LIGHTING_DB_DRIVER`
  - `LIGHTING_STORAGE_TYPE`（默认 `jdbc`）
  - `LIGHTING_CONSOLE_TOKENS`（控制台/客户端 Token，默认 `lighting-console-token`）
  - 如修改数据库名，请同步调整 `LIGHTING_DB_URL` 的数据库段

## 外部数据库部署

- 模板：`docker/docker-compose.external-db.yml`，仅包含 Server，使用外部 DB（PostgreSQL/MySQL/Oracle 均可）。
- 配置示例（`.env`）：
  ```env
  LIGHTING_DB_URL=jdbc:postgresql://host.docker.internal:5432/lighting_config
  LIGHTING_DB_DRIVER=org.postgresql.Driver
  LIGHTING_DB_USER=lighting
  LIGHTING_DB_PASSWORD=lighting
  LIGHTING_CONSOLE_TOKENS=lighting-console-token
  SERVER_PORT=7086
  ```
- 启动：
  ```bash
  docker compose -f docker/docker-compose.external-db.yml up -d
  ```
- 与 Nacos 类似，数据库连接信息在 `.env` 中集中管理，可切换 MySQL 时修改 `LIGHTING_DB_URL` 与 `LIGHTING_DB_DRIVER`（例如 `com.mysql.cj.jdbc.Driver`）。

## 运行检查

- 健康检查：`http://localhost:7086/actuator/health`
- API/控制台入口：
  - OpenAPI：`http://localhost:7086/lighting-config/api/docs`
  - 控制台：`http://localhost:7086/lighting-config/index.html`，默认 Token `lighting-console-token`

## 注意事项

- 默认容器 JRE 版本为 17，与当前 Maven 构建设置保持一致（源码依旧遵循 JDK 11 语法约束）。如需更低版本运行，请先以 JDK 11 编译并调整 Docker 基础镜像。
- 数据库初始化：当前依赖外部初始化脚本（见 `docs/schema/`），后续可接入 Flyway/Liquibase 做自动建表。
