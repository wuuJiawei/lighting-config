# lighting-config-console

Lighting Config 的 Web 控制台，基于 Vite + React + TypeScript 构建，落实 `docs/frontend-architecture.md` 中的路由、状态与 UI 规划，服务于配置 CRUD、命名空间治理与审计可视化场景。

## 技术栈
- Vite 7 + React 19 + TypeScript 5
- Tailwind CSS + shadcn/ui（Radix primitives）
- TanStack Query 5 管理后端数据，Zustand 管理本地 UI 状态
- React Hook Form + zod 做表单校验
- Vitest + Testing Library 提供单测能力

## 常用脚本
| 命令 | 说明 |
| --- | --- |
| `pnpm dev` | 本地开发，默认代理到 `http://localhost:8080` 的 `/lighting-config/api` 与 `/actuator` |
| `pnpm test` / `pnpm test:watch` | 运行 Vitest 单测 |
| `pnpm lint` / `pnpm lint:style` | ESLint（含 type-aware 规则）与 Stylelint |
| `pnpm build` | `tsc` 检查 + Vite 打包，随后将 `dist/` 同步到 `lighting-config-server/src/main/resources/static/console` |
| `pnpm preview` | 生产包本地预览 |

> Node.js ≥ 20.11 + pnpm ≥ 8.15。首次安装依赖请运行 `pnpm install`。

## 目录结构
```
src/
├── app/               # 路由、layout、Providers
├── api/               # Axios 客户端、数据契约、mock fallback
├── components/
│   ├── shared/        # PageHeader、ConfigTable 等复合组件
│   └── ui/            # Tailwind + shadcn 基础组件
├── hooks/             # useConfigFilters 等自定义 hook
├── pages/             # Dashboard / ConfigList / ConfigEditor / Namespace / Audit
├── stores/            # Zustand 切片
├── styles/            # Tailwind 主题 & CSS token
└── tests/             # Vitest setup 与示例
```

## 与 Server 的联动
- 默认通过同域 `/lighting-config/api` 调用 `lighting-config-server`，本地开发由 Vite Proxy 转发到 `http://localhost:8080`。
- 登录采用轻量 token 模式，向 `/lighting-config/api/auth/login` 提交令牌（默认 `lighting-console-token`，可通过 `lighting.config.auth.options.tokens` 配置）。成功后浏览器会在 LocalStorage 持久化，所有请求均携带 `Authorization: Bearer <token>`。
- `pnpm build` 会自动调用 `scripts/sync-server-static.mjs`，将前端静态资源复制到 `lighting-config-server/src/main/resources/static/console`，方便 Spring Boot 直接托管（如不希望提交构建产物，可在提交前删除该目录）。
- 若后端 API 仍在开发阶段，TanStack Query 会自动回落到 `src/api/mocks.ts` 中的示例数据，确保 UI 可预览。

## 下一步建议
- 与 Server Agent 对齐 `/api/config`、`/api/namespace`、`/api/audit` 的最终 DTO，并在 `src/api` 中替换 mock。
- 扩展登录/鉴权（Token/JWT）并绑定顶部账户菜单。
- 引入 E2E（Cypress）与更细粒度的组件单测，纳入 Quality Agent 的测试矩阵。
