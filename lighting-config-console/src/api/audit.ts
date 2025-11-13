import { apiClient, withApiFallback } from './client'
import { mockAuditTrail } from './mocks'
import type { AuditRecord } from './types'

export interface AuditQuery {
  limit?: number
  tenant?: string
}

export async function fetchAuditTrail(params: AuditQuery = {}): Promise<AuditRecord[]> {
  const query = {
    tenant: params.tenant ?? 'default',
    limit: params.limit ?? 20,
  }

  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<AuditRecord[]>('/console/audit', { params: query })
      return data
    },
    mockAuditTrail,
    'audit:get',
  )
}
