import { apiClient, withApiFallback } from './client'
import { ADMIN_CONSOLE_NAMESPACES } from './routes'
import { mockNamespaces } from './mocks'
import type { NamespaceSummary } from './types'

export async function fetchNamespaces(tenant = 'default'): Promise<NamespaceSummary[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<NamespaceSummary[]>(ADMIN_CONSOLE_NAMESPACES, {
        params: { tenant },
      })
      return data
    },
    mockNamespaces,
    'namespace:get',
  )
}
