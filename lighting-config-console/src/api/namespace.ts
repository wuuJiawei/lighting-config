import { apiClient, withApiFallback } from './client'
import { mockNamespaces } from './mocks'
import type { NamespaceSummary } from './types'

export async function fetchNamespaces(tenant = 'default'): Promise<NamespaceSummary[]> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<NamespaceSummary[]>('/console/namespaces', {
        params: { tenant },
      })
      return data
    },
    mockNamespaces,
    'namespace:get',
  )
}
