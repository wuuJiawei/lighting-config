# config_item 表字段参考

`config_item` 是 lighting-config 的权威存储，落地在 `docs/schema/*.sql` 中。表结构面向“租户/命名空间/应用/键”四维唯一约束，其余字段描述配置的格式、状态与附加上下文。下表列出了主要列的含义与默认值。

| 字段 | 数据类型（例：PostgreSQL） | 默认值 | 说明 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | 自增 | 技术主键。绝大多数查询走 `(tenant, namespace, app_id, key)` 唯一键，`id` 仅用于内部引用。 |
| `tenant` | `VARCHAR(64)` | `default` | 逻辑租户，用于跨环境或跨业务线隔离。 |
| `namespace` | `VARCHAR(128)` | 必填 | 同一租户下的命名空间（例如 `prod`、`staging`、`finance`）。 |
| `app_id` | `VARCHAR(128)` | 必填 | 该条配置所属应用或作用域。`__global__` 为保留值，表示所有应用共享。 |
| `key` | `VARCHAR(512)` | 必填 | 配置键，和 `tenant/namespace/app_id` 组成唯一索引。 |
| `content_type` | `VARCHAR(32)` | `STRING` | 值的数据类型，枚举见下文。控制台和 SDK 会使用它驱动编辑器/自动解析。 |
| `value` | `TEXT/JSON/CLOB` | 必填 | 实际配置内容，按照 `content_type` 解释。 |
| `version` | `BIGINT` | 1 | 递增版本号。每次 upsert 自动 +1，客户端用它判断增量。 |
| `tags` | `JSONB` | `NULL` | `Map<String,String>`，存放标签（例如 `env=prod`、`owner=risk`）。用于筛选、灰度、审计。为空时写入 `NULL`。 |
| `enabled` | `BOOLEAN` | `TRUE` | 是否有效；删除时会将该标记置为 `false` 并生成 `ChangeType.DELETE`。 |
| `created_at` | `TIMESTAMPTZ` | `NOW()` | 创建时间。 |
| `updated_at` | `TIMESTAMPTZ` | `NOW()` | 最近一次修改时间。 |

> 唯一键：`UNIQUE (tenant, namespace, app_id, key)`，因此任何组合只能存在一条记录。删除/禁用会保留历史在 `revision` 表中。

## content_type 枚举

`lighting-config-core` 中的 `ContentType` 描述了值的语义类型，而不是纯文本格式。支持以下枚举：

| 枚举常量 | 说明 | 示例 |
| --- | --- | --- |
| `STRING` | 任意字符串或多行文本，兼容旧的 `TEXT/YAML/PROPERTIES` 值。 | `jdbc:mysql://…`、YAML 片段 |
| `BOOLEAN` | 布尔型，客户端会自动解析为 `boolean`/`Boolean`。 | `true`、`false` |
| `BYTE` / `SHORT` / `INTEGER` / `LONG` | 整型数值，对应 Java 基本数据类型。 | `42`、`1024` |
| `FLOAT` / `DOUBLE` | 浮点型数值。 | `3.14`、`0.618` |
| `LIST` | JSON 数组，支持嵌套 `List<Integer>`、`List<Map<String,Object>>` 等。 | `[true,false,true]` |
| `MAP` | JSON 对象，等价于 `Map<String,Object>`，支持嵌套结构。 | `{"enabled":true,"weight":0.3}` |

解析规则：

1. 控制台依据 `content_type` 选择输入组件，例如布尔开关、JSON 编辑器。
2. SDK 在 `PollResponse`／`ConfigChange` 中透出 `contentType`，并结合 `LightingValue`/`LightingProperties` 的目标类型自动解析成 Java 对象。
3. 如果写入未知类型，系统会回退到 `STRING`；旧数据中的 `TEXT/JSON/YAML/PROPERTIES` 别名仍然会被接受。

## tags 使用建议

`tags` 是可选的 JSON 标签，常见用途：

- **多维检索**：控制台/REST API 支持通过标签过滤记录，快速定位 `env=prod`、`owner=ops` 等配置。
- **灰度策略**：未来可与 Selector/SPI 结合，实现“只下发 `region=apac` 的配置”。
- **审计**：`revision` 与 `audit_log` 会把标签一起记录，帮助定位一次发布影响的范围。

写入方式：

- Server 端将 `Map<String,String>` 序列化为 JSON；空集合写 `NULL`。
- JDBC 实现（`JdbcConfigRepository`）在查询时会反序列化为不可变 `Map`。
- 客户端当前不直接消费标签；它们只在服务端用于管理/筛选。

## 示例

```sql
INSERT INTO config_item (tenant, namespace, app_id, key, content_type, value, version, tags, enabled)
VALUES (
  'default', 'prod', 'order-service', 'feature.pay.v2', 'JSON',
  '{"enabled":true,"rate":0.7}',
  3,
  '{"env":"prod","owner":"fintech"}',
  TRUE
);
```

- 若要声明全局可用配置，将 `app_id` 设置为 `__global__`；客户端自动在业务 `appId` 后追加该 scope 并合并。
- 删除时建议使用 API/控制台触发，这样 `revision` 会记录一条 `ChangeType.DELETE` 方便回滚。
