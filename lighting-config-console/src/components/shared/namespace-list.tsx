import type { NamespaceSummary } from '@/api/types'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { formatDateTime, formatRelative } from '@/utils/date'

interface NamespaceAlertMap {
  [namespace: string]: {
    total: number
    latest: string
  }
}

interface NamespaceListProps {
  namespaces: NamespaceSummary[]
  alertsByNamespace?: NamespaceAlertMap
}

export function NamespaceList({ namespaces, alertsByNamespace = {} }: NamespaceListProps) {
  if (!namespaces.length) {
    return <p className="text-sm text-muted-foreground">暂无命名空间</p>
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      {namespaces.map((ns) => {
        const alertInfo = alertsByNamespace[ns.name]
        return (
          <Card key={ns.id}>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                {ns.name}
                <span className="text-sm font-normal text-muted-foreground">{ns.configCount} configs</span>
              </CardTitle>
              <CardDescription>Owner · {ns.owner}</CardDescription>
              {alertInfo ? (
                <div className="flex items-center gap-2">
                  <Badge variant="destructive" className="text-xs">
                    缓存告警 {alertInfo.total}
                  </Badge>
                  <span className="text-xs text-muted-foreground">上次 {formatRelative(alertInfo.latest)}</span>
                </div>
              ) : (
                <span className="text-xs text-muted-foreground">缓存命中正常</span>
              )}
            </CardHeader>
            <CardContent className="space-y-2 text-sm text-muted-foreground">
              <div>App ID：{ns.appIds.join(' / ')}</div>
              <div>启用配置：{ns.watchers}</div>
              <div>最近更新：{formatDateTime(ns.updatedAt)}</div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
