import type {
  AuditRecord,
  CacheMissAlert,
  ConfigItem,
  ConfigListResponse,
  ConfigRevision,
  DashboardStat,
  NamespaceSummary,
} from './types'

export const mockConfigs: ConfigItem[] = [
  {
    id: 'default::default::lighting-console::feature.toggle.push',
    tenant: 'default',
    namespace: 'default',
    appId: 'lighting-console',
    key: 'feature.toggle.push',
    value: 'push.enabled=true',
    contentType: 'STRING',
    labels: { push: 'true', gray: 'true' },
    enabled: true,
    version: 17,
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'default::default::lighting-console::poll.interval.ms',
    tenant: 'default',
    namespace: 'default',
    appId: 'lighting-console',
    key: 'poll.interval.ms',
    value: 'poll.interval.ms=15000',
    contentType: 'STRING',
    labels: { polling: 'true' },
    enabled: true,
    version: 8,
    updatedAt: new Date(Date.now() - 3600_000).toISOString(),
  },
  {
    id: 'default::beta::lighting-console::db.pool.size',
    tenant: 'default',
    namespace: 'beta',
    appId: 'lighting-console',
    key: 'db.pool.size',
    value: 'db.pool.size=32',
    contentType: 'STRING',
    labels: { database: 'true' },
    enabled: false,
    version: 2,
    updatedAt: new Date(Date.now() - 86_400_000).toISOString(),
  },
]

export const mockConfigList: ConfigListResponse = {
  items: mockConfigs,
  total: mockConfigs.length,
}

export const mockNamespaces: NamespaceSummary[] = [
  {
    id: 'ns-default',
    name: 'default',
    owner: 'Server Platform',
    configCount: 42,
    watchers: 180,
    appIds: ['lighting-console'],
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'ns-beta',
    name: 'beta',
    owner: 'Client Agent',
    configCount: 18,
    watchers: 45,
    appIds: ['lighting-console'],
    updatedAt: new Date(Date.now() - 2000_000).toISOString(),
  },
]

export const mockAuditTrail: AuditRecord[] = [
  {
    id: 'audit-001',
    configKey: 'feature.toggle.push',
    namespace: 'default',
    appId: 'lighting-console',
    operator: 'ops-bot',
    action: 'PUBLISH',
    message: '发布生产版本 v17',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'audit-002',
    configKey: 'poll.interval.ms',
    namespace: 'default',
    appId: 'lighting-console',
    operator: 'architect',
    action: 'DELETE',
    message: '移除灰度配置',
    createdAt: new Date(Date.now() - 7200_000).toISOString(),
  },
]

export const mockDashboardStats: DashboardStat[] = [
  { label: '活跃配置', value: '128', change: 6.3, trend: 'up', hint: '最近 24 小时' },
  { label: '拉取请求', value: '18.2k', change: 2.1, trend: 'up', hint: '过去 60 分钟' },
  { label: '灰度实例', value: '42', change: -1.2, trend: 'down', hint: '进行中灰度' },
  { label: '失败推送', value: '3', change: -66.0, trend: 'down', hint: '事件中心' },
]

export const mockCacheMissAlerts: CacheMissAlert[] = [
  {
    id: 'miss-01',
    namespace: 'prod',
    appId: 'order-service',
    selector: 'datasource.',
    missCount: 7,
    occurredAt: new Date().toISOString(),
  },
  {
    id: 'miss-02',
    namespace: 'prod',
    appId: 'inventory-service',
    selector: '*',
    missCount: 5,
    occurredAt: new Date(Date.now() - 9_000_00).toISOString(),
  },
]

export const mockRevisions: ConfigRevision[] = [
  {
    id: 'rev-001',
    tenant: 'default',
    namespace: 'default',
    appId: 'lighting-console',
    key: 'feature.toggle.push',
    version: 17,
    op: 'UPSERT',
    operator: 'ops-bot',
    diff: '开启推送',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'rev-002',
    tenant: 'default',
    namespace: 'default',
    appId: 'lighting-console',
    key: 'feature.toggle.push',
    version: 16,
    op: 'UPSERT',
    operator: 'qa',
    diff: '灰度开启',
    createdAt: new Date(Date.now() - 86_400_000).toISOString(),
  },
  {
    id: 'rev-003',
    tenant: 'default',
    namespace: 'default',
    appId: 'lighting-console',
    key: 'feature.toggle.push',
    version: 15,
    op: 'DELETE',
    operator: 'ops-bot',
    diff: '清理默认值',
    createdAt: new Date(Date.now() - 2 * 86_400_000).toISOString(),
  },
]
