import { apiClient, withApiFallback } from './client'
import { ADMIN_CONSOLE_AUDIT, ADMIN_CONSOLE_CACHE_MISS, ADMIN_CONSOLE_STATS } from './routes'
import { mockAuditTrail, mockCacheMissAlerts, mockDashboardStats } from './mocks'
import type { AuditRecord, CacheMissAlert, DashboardStat } from './types'

const DEFAULT_TENANT = 'default'

export async function fetchDashboardStats(tenant = DEFAULT_TENANT): Promise<DashboardStat[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<DashboardStat[]>(ADMIN_CONSOLE_STATS, { params: { tenant } })
      return data
    },
    mockDashboardStats,
    'dashboard:stats:get',
  )
}

export async function fetchRecentAudits(limit = 5, tenant = DEFAULT_TENANT): Promise<AuditRecord[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<AuditRecord[]>(ADMIN_CONSOLE_AUDIT, { params: { limit, tenant } })
      return data
    },
    mockAuditTrail,
    'dashboard:audit:get',
  )
}

export async function fetchCacheMissAlerts(limit = 10, tenant = DEFAULT_TENANT): Promise<CacheMissAlert[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<CacheMissAlert[]>(ADMIN_CONSOLE_CACHE_MISS, { params: { limit, tenant } })
      return data
    },
    mockCacheMissAlerts,
    'dashboard:cache-miss:get',
  )
}
