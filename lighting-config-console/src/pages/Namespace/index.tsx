import { useQuery } from '@tanstack/react-query'
import { fetchNamespaces } from '@/api/namespace'
import type { NamespaceSummary } from '@/api/types'
import { PageHeader } from '@/components/shared/page-header'
import { NamespaceList } from '@/components/shared/namespace-list'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function NamespacePage() {
  const { data = [], isLoading } = useQuery<NamespaceSummary[]>({
    queryKey: ['namespaces', 'default'],
    queryFn: () => fetchNamespaces('default'),
  })
  const totalWatchers = data.reduce((sum, ns) => sum + ns.watchers, 0)

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
            <CardTitle>双形态配置</CardTitle>
            <CardDescription>HTTP + 本地缓存</CardDescription>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            命名空间级别的推送策略可在配置中心 + 嵌入式客户端之间切换。
          </CardContent>
        </Card>
      </div>
      {isLoading ? <p className="text-sm text-muted-foreground">加载中...</p> : <NamespaceList namespaces={data} />}
    </div>
  )
}
