import { apiClient, withApiFallback } from './client'
import { mockAuditTrail, mockDashboardStats } from './mocks'
import type { AuditRecord, DashboardStat } from './types'

const DEFAULT_TENANT = 'default'

export async function fetchDashboardStats(tenant = DEFAULT_TENANT): Promise<DashboardStat[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<DashboardStat[]>('/console/stats', { params: { tenant } })
      return data
    },
    mockDashboardStats,
    'dashboard:stats:get',
  )
}

export async function fetchRecentAudits(limit = 5, tenant = DEFAULT_TENANT): Promise<AuditRecord[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<AuditRecord[]>('/console/audit', { params: { limit, tenant } })
      return data
    },
    mockAuditTrail,
    'dashboard:audit:get',
  )
}
