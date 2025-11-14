import type { CacheMissAlert } from '@/api/types'
import { formatRelative } from '@/utils/date'
import { Badge } from '@/components/ui/badge'

interface CacheMissAlertListProps {
  alerts: CacheMissAlert[]
}

export function CacheMissAlertList({ alerts }: CacheMissAlertListProps) {
  if (!alerts || alerts.length === 0) {
    return <p className="text-sm text-muted-foreground">最近 24 小时暂无缓存回源。</p>
  }

  return (
    <ul className="space-y-3">
      {alerts.map((alert) => (
        <li key={alert.id ?? `${alert.namespace}-${alert.appId}-${alert.selector}-${alert.occurredAt}`}>
          <div className="flex items-start justify-between gap-4 rounded-md border border-border/60 px-3 py-2">
            <div>
              <p className="font-medium">
                {alert.namespace} · {alert.appId}
              </p>
              <p className="text-xs text-muted-foreground">匹配：{alert.selector || '*'}</p>
            </div>
            <div className="text-right">
              <Badge variant="destructive" className="text-xs">
                {alert.missCount} 次
              </Badge>
              <p className="mt-1 text-xs text-muted-foreground">{formatRelative(alert.occurredAt)}</p>
            </div>
          </div>
        </li>
      ))}
    </ul>
  )
}
