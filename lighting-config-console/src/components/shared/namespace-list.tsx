import type { NamespaceSummary } from '@/api/types'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatDateTime } from '@/utils/date'

interface NamespaceListProps {
  namespaces: NamespaceSummary[]
}

export function NamespaceList({ namespaces }: NamespaceListProps) {
  if (!namespaces.length) {
    return <p className="text-sm text-muted-foreground">暂无命名空间</p>
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {namespaces.map((ns) => (
        <Card key={ns.id}>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              {ns.name}
              <span className="text-sm font-normal text-muted-foreground">{ns.configCount} configs</span>
            </CardTitle>
            <CardDescription>Owner · {ns.owner}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-sm text-muted-foreground">
            <div>App ID：{ns.appIds.join(' / ')}</div>
            <div>启用配置：{ns.watchers}</div>
            <div>最近更新：{formatDateTime(ns.updatedAt)}</div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
