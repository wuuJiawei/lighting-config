import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { fetchNamespaces } from '@/api/namespace'
import { fetchCacheMissAlerts } from '@/api/dashboard'
import type { CacheMissAlert, NamespaceSummary } from '@/api/types'
import { PageHeader } from '@/components/shared/page-header'
import { NamespaceList } from '@/components/shared/namespace-list'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function NamespacePage() {
  const { data = [], isLoading } = useQuery<NamespaceSummary[]>({
    queryKey: ['namespaces', 'default'],
    queryFn: () => fetchNamespaces('default'),
  })
  const { data: cacheMissAlerts = [] } = useQuery<CacheMissAlert[]>({
    queryKey: ['namespaces', 'cache-miss'],
    queryFn: () => fetchCacheMissAlerts(50),
    refetchInterval: 60_000,
  })

  const totalWatchers = data.reduce((sum, ns) => sum + ns.watchers, 0)
  const namespaceAlerts = useMemo(() => aggregateNamespaceAlerts(cacheMissAlerts), [cacheMissAlerts])
  const affectedNamespaces = Object.keys(namespaceAlerts).length
  const totalCacheMissEvents = cacheMissAlerts.reduce((sum, alert) => sum + alert.missCount, 0)

  return (
    <div className="space-y-6">
      <PageHeader title="命名空间" description="管理隔离域、租户及订阅关系。" />
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>{data.length}</CardTitle>
            <CardDescription>总命名空间</CardDescription>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>{totalWatchers}</CardTitle>
            <CardDescription>累计监听实例</CardDescription>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>{affectedNamespaces}</CardTitle>
            <CardDescription>缓存异常命名空间</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            {affectedNamespaces > 0
              ? `最近记录 ${totalCacheMissEvents} 次缓存回源，优先排查高频命名空间。`
              : '全部命名空间缓存命中正常。'}
          </CardContent>
        </Card>
      </div>
      {isLoading ? (
        <p className="text-sm text-muted-foreground">加载中...</p>
      ) : (
        <NamespaceList namespaces={data} alertsByNamespace={namespaceAlerts} />
      )}
    </div>
  )
}

type NamespaceAlertAggregation = Record<
  string,
  {
    total: number
    latest: string
  }
>

function aggregateNamespaceAlerts(alerts: CacheMissAlert[]): NamespaceAlertAggregation {
  return alerts.reduce<NamespaceAlertAggregation>((acc, alert) => {
    const namespace = alert.namespace
    const existing = acc[namespace]
    if (existing) {
      existing.total += alert.missCount
      if (new Date(alert.occurredAt).getTime() > new Date(existing.latest).getTime()) {
        existing.latest = alert.occurredAt
      }
    } else {
      acc[namespace] = { total: alert.missCount, latest: alert.occurredAt }
    }
    return acc
  }, {})
}
