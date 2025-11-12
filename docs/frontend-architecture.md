# lighting-config 控制台前端架构草案

## 1. 目标
- 为 `lighting-config-server` 提供基础 Web 控制台：配置 CRUD、监听状态、命名空间/租户管理、推送日志可视化。
- 与后端 REST API (`/api/config`, `/api/gray`, `/api/poll`, `/actuator`) 对齐，后续可扩展到 WebSocket/SSE。
- 设计可渐进增强的项目骨架，方便后续 Agent/Contributor 直接补充页面。

## 2. 技术栈
- **构建**：Vite 6 + TypeScript 5，保持快速冷启动与出色的 HMR 体验，可无缝接入 Vitest/Cypress。
- **框架**：React 19 + React Router 7。
- **状态管理**：
  - TanStack Query 5 负责远程数据获取与缓存（分页、轮询、错误边界等）。
  - Zustand 5 管理本地 UI/表单状态（右侧抽屉、临时筛选、向导步骤等），保持比 Redux/MobX 更轻量。
- **UI 体系**：shadcn/ui（Radix UI primitives + Tailwind CSS）。提供无样式但可组合的组件，我们自定义主题以贴合 lighting-config 的观感，避免 Ant Design 重皮肤&体积负担。
- **表单**：React Hook Form + zod schema，联合 shadcn Form 组件，统一校验与类型安全。
- **可观测**：ESLint（flat config）+ Prettier + Stylelint，Vitest 做单元/组件测试，Cypress 负责关键 E2E。

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
│   ├── components/
│   │   ├── ui/                 # shadcn 生成的基础组件(Button/Input/Dialog...)
│   │   └── shared/             # 复合组件（表格、搜索栏、EmptyState 等）
│   ├── api/
│   │   ├── client.ts           # axios 封装 + 拦截器
│   │   └── config.ts           # `/api/config` 请求封装
│   ├── hooks/                  # useConfigList/useTenantSelect/usePollingSwitch 等
│   ├── stores/                 # Zustand slices（layout、config-editor 等）
│   ├── utils/
│   └── styles/                 # Tailwind 基础样式、设计 Token
└── tests/
    ├── unit/
    └── e2e/
```

## 4. 数据流 & 通信
- 通过 REST API 与 server 交互，默认 baseURL=`/api`（同域部署，可由 nginx/Spring 网关负责反向代理）。
- TanStack Query 负责服务端数据，内置 `queryClient` 处理分页/重试/缓存失效；Mutation 成功后局部 `invalidate`.
- Zustand 以 slice 形式管理 UI 状态（筛选条件、抽屉显隐、草稿 config），并暴露 hooks 给页面层。避免在 Query 中塞入 UI state。
- 轮询结果与动态变更目前通过 HTTP `/api/poll` 完成，未来如需更实时可扩展 WebSocket/SSE Gateway。

## 5. 开发/构建脚本（计划）
| 命令 | 说明 |
| --- | --- |
| `pnpm dev` | 本地开发，自带代理到 `http://localhost:8080` |
| `pnpm test` | Vitest 单元/组件测试 |
| `pnpm lint` | ESLint + Stylelint |
| `pnpm build` | 产出 `dist/`，供 Spring Boot 或 Nginx 托管 |
| `pnpm preview` | 本地预览生产包 |
| `pnpm shadcn:add <component>` | 复用 shadcn/ui 生成的 Radix 封装组件 |

> 依赖管理采用 `pnpm`，Lockfile 提交到仓库，Node 版本锁定 20.x。

## 6. 部署模式
1. **嵌入式**：`lighting-config-server` 打包时通过 Maven `frontend-maven-plugin` 编译前端并放入 `src/main/resources/static`。
2. **独立部署**：`lighting-config-console` 产出的 `dist/` 由 Nginx/OSS 托管，通过反向代理走后端 API。

## 7. 下一步
- 在 `lighting-config-console/` 目录初始化 Vite 工程（本次仅创建占位 README）。
- 与 Server Agent 对齐 REST API 分页/过滤参数，补齐 Swagger/JSON Schema，方便自动生成类型。
- 设计权限模型映射（Token + 菜单粒度），准备后续接入。
