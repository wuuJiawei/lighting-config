import { useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import type { ConfigListResponse } from '@/api/types'
import { fetchConfigList } from '@/api/config'
import { fetchNamespaces } from '@/api/namespace'
import { ConfigTable } from '@/components/shared/config-table'
import { ConfigFilterBar } from '@/components/shared/config-filter-bar'
import { PageHeader } from '@/components/shared/page-header'
import { Button } from '@/components/ui/button'
import { useConfigFilters } from '@/hooks/useConfigFilters'

export function ConfigListPage() {
  const navigate = useNavigate()
  const { filters, updateFilters } = useConfigFilters()

  const namespacesQuery = useQuery({
    queryKey: ['namespaces', filters.tenant],
    queryFn: () => fetchNamespaces(filters.tenant ?? 'default'),
  })

  const queryInput = useMemo(
    () => ({
      tenant: filters.tenant,
      namespace: filters.namespace,
      appId: filters.appId,
      keyword: filters.keyword,
    }),
    [filters],
  )

  const { data, isLoading } = useQuery<ConfigListResponse>({
    queryKey: ['configs', queryInput],
    queryFn: () => fetchConfigList(queryInput),
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="配置列表"
        description="查看、过滤并快速定位命名空间内的配置项。"
        actions={
          <Button onClick={() => void navigate('/configs/new')}>
            新建配置
          </Button>
        }
      />
      <ConfigFilterBar
        filters={filters}
        onChange={updateFilters}
        namespaces={
          namespacesQuery.data ?? [
            {
              id: 'default',
              name: 'default',
              owner: '-',
              configCount: 0,
              watchers: 0,
              appIds: ['default'],
              updatedAt: new Date().toISOString(),
            },
          ]
        }
        isLoading={isLoading}
      />
      <ConfigTable items={data?.items ?? []} isLoading={isLoading} />
    </div>
  )
}
