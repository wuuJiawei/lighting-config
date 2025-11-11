# lighting-config 控制台前端架构草案

## 1. 目标
- 为 `lighting-config-server` 提供基础 Web 控制台：配置 CRUD、监听状态、命名空间/租户管理、推送日志可视化。
- 与后端 REST API (`/api/config`, `/api/gray`, `/actuator`) 对齐，后续可扩展到 gRPC WebProxy。
- 设计可渐进增强的项目骨架，方便后续 Agent/Contributor 直接补充页面。

## 2. 技术栈
- **构建**：Vite 6 + TypeScript 5（更快的本地开发体验，可轻松接入 Vitest/Cypress）。
- **框架**：React 19 + React Router 7。
- **状态管理**：TanStack Query 5（数据获取 + 缓存），对表单/局部状态使用 React Hook Form。
- **UI 体系**：Ant Design 5（主题可定制，表格/表单组件齐全），搭配 CSS Modules + PostCSS。
- **可观测**：集成 ESLint + Stylelint + Prettier，Vitest 做单元/组件测试，Cypress 处理关键 E2E。

## 3. 项目结构（计划）
```
lighting-config-console/
├── package.json
├── vite.config.ts
├── src/
│   ├── main.tsx                # 入口，注入路由/QueryClient
│   ├── app/
│   │   ├── routes.tsx          # React Router 声明
│   │   └── providers.tsx       # 全局 Provider（主题、Query、Auth）
│   ├── pages/
│   │   ├── Dashboard/
│   │   ├── ConfigList/
│   │   ├── ConfigEditor/
│   │   ├── Namespace/
│   │   └── Audit/
│   ├── components/             # 共享 UI（表格、搜索栏、EmptyState 等）
│   ├── api/
│   │   ├── client.ts           # axios 封装 + 拦截器
│   │   └── config.ts           # `/api/config` 请求封装
│   ├── hooks/                  # useConfigList/useTenantSelect 等
│   ├── stores/                 # 未来若需要 Zustand/Context
│   ├── utils/
│   └── styles/
└── tests/
    ├── unit/
    └── e2e/
```

## 4. 数据流 & 通信
- 通过 REST API 与 server 交互，默认 baseURL=`/api`（同域部署，依赖反向代理解决跨域）。
- TanStack Query 负责请求缓存、刷新、错误边界；Mutation 成功后自动刷新对应列表。
- SSE/gRPC 推送暂不直接接入，后续可通过 WebSocket Gateway 拓展。

## 5. 开发/构建脚本（计划）
| 命令 | 说明 |
| --- | --- |
| `pnpm dev` | 本地开发，自带代理到 `http://localhost:8080` |
| `pnpm test` | Vitest 单元/组件测试 |
| `pnpm lint` | ESLint + Stylelint |
| `pnpm build` | 产出 `dist/`，供 Spring Boot 或 Nginx 托管 |
| `pnpm preview` | 本地预览生产包 |

> 依赖管理采用 `pnpm`，Lockfile 提交到仓库，Node 版本锁定 20.x。

## 6. 部署模式
1. **嵌入式**：`lighting-config-server` 打包时通过 Maven `frontend-maven-plugin` 编译前端并放入 `src/main/resources/static`。
2. **独立部署**：`lighting-config-console` 产出的 `dist/` 由 Nginx/OSS 托管，通过反向代理走后端 API。

## 7. 下一步
- 在 `lighting-config-console/` 目录初始化 Vite 工程（本次仅创建占位 README）。
- 与 Server Agent 对齐 REST API 分页/过滤参数，补齐 Swagger/JSON Schema，方便自动生成类型。
- 设计权限模型映射（Token + 菜单粒度），准备后续接入。
