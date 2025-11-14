import { apiClient, withApiFallback } from './client'
import { ADMIN_CONSOLE_AUDIT } from './routes'
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
      const { data } = await apiClient.get<AuditRecord[]>(ADMIN_CONSOLE_AUDIT, { params: query })
      return data
    },
    mockAuditTrail,
    'audit:get',
  )
}
