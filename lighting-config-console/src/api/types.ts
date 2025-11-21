export type ConfigStatus = 'ACTIVE' | 'DISABLED'

export interface ConfigItem {
  id: string
  tenant: string
  namespace: string
  appId: string
  key: string
  value: string
  contentType: string
  labels: Record<string, string>
  enabled: boolean
  version: number
  updatedAt: string
}

export interface ConfigListResponse {
  items: ConfigItem[]
  total: number
}

export interface ConfigUpsertPayload {
  tenant: string
  namespace: string
  appId: string
  key: string
  value: string
  contentType: string
  labels: Record<string, string>
  enabled: boolean
}

export interface NamespaceSummary {
  id: string
  name: string
  owner: string
  configCount: number
  watchers: number
  appIds: string[]
  updatedAt: string
}

export interface AuditRecord {
  id: string
  configKey: string
  namespace: string
  appId: string
  operator: string
  action: 'PUBLISH' | 'DELETE'
  message: string
  createdAt: string
}

export interface DashboardStat {
  label: string
  value: string
  change: number
  trend: 'up' | 'down'
  hint?: string
}

export interface CacheMissAlert {
  id?: string
  namespace: string
  appId: string
  selector: string
  missCount: number
  occurredAt: string
}

export interface ConfigRevision {
  id?: string
  tenant?: string
  namespace?: string
  appId?: string
  key?: string
  version: number
  op: 'UPSERT' | 'DELETE'
  operator?: string
  diff?: string
  createdAt: string
}
