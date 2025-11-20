import { useEffect, useMemo } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
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
  const location = useLocation()
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

  const tenantFilter = filters.tenant
  const namespaceFilter = filters.namespace
  const appIdFilter = filters.appId
  const keywordFilter = filters.keyword

  useEffect(() => {
    const namespaces = namespacesQuery.data && namespacesQuery.data.length ? namespacesQuery.data : [
      {
        id: 'default',
        name: 'default',
        owner: '-',
        configCount: 0,
        watchers: 0,
        appIds: ['__global__'],
        updatedAt: new Date().toISOString(),
      },
    ]
    const tenantValue = tenantFilter ?? 'default'
    const namespaceExists = namespaces.some((ns) => ns.name === namespaceFilter)
    const namespaceValue = namespaceExists ? (namespaceFilter as string) : namespaces[0].name
    const selectedNamespace = namespaces.find((ns) => ns.name === namespaceValue) ?? namespaces[0]
    const appExists = selectedNamespace.appIds.includes(appIdFilter ?? '')
    const appValue = appExists ? (appIdFilter as string) : selectedNamespace.appIds[0] ?? '__global__'
    if (tenantValue !== tenantFilter || namespaceValue !== namespaceFilter || appValue !== appIdFilter) {
      updateFilters({
        tenant: tenantValue,
        namespace: namespaceValue,
        appId: appValue,
        keyword: keywordFilter ?? '',
      })
    }
  }, [tenantFilter, namespaceFilter, appIdFilter, keywordFilter, namespacesQuery.data, updateFilters])

  const listState = { from: { pathname: location.pathname, search: location.search } }

  return (
    <div className="space-y-6">
      <PageHeader
        title="配置列表"
        description="查看、过滤并快速定位命名空间内的配置项。"
        actions={
          <Button onClick={() => void navigate('/configs/new', { state: listState })}>
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
              appIds: ['__global__'],
              updatedAt: new Date().toISOString(),
            },
          ]
        }
        isLoading={isLoading}
      />
      <ConfigTable items={data?.items ?? []} isLoading={isLoading} listState={listState} />
    </div>
  )
}
