import type { AuditRecord } from '@/api/types'
import { formatDateTime } from '@/utils/date'
import { Badge } from '@/components/ui/badge'

const ACTION_COLORS: Record<AuditRecord['action'], 'success' | 'destructive'> = {
  PUBLISH: 'success',
  DELETE: 'destructive',
}

interface AuditTimelineProps {
  records: AuditRecord[]
}

export function AuditTimeline({ records }: AuditTimelineProps) {
  if (!records.length) {
    return <p className="text-sm text-muted-foreground">暂无审计记录</p>
  }

  return (
    <ol className="space-y-4">
      {records.map((record) => (
        <li key={record.id} className="relative flex gap-4 border-l pl-6">
          <span className="absolute left-[-6px] top-1 h-3 w-3 rounded-full border-2 border-primary bg-background" />
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <Badge variant={ACTION_COLORS[record.action]}>{record.action}</Badge>
              <span className="font-medium">{record.namespace}</span>
              <span className="text-muted-foreground">{record.appId}</span>
              <span className="text-muted-foreground">{record.configKey}</span>
            </div>
            <p className="text-sm text-muted-foreground">{record.message}</p>
            <p className="text-xs text-muted-foreground">{record.operator}</p>
          </div>
          <div className="ml-auto text-xs text-muted-foreground">{formatDateTime(record.createdAt)}</div>
        </li>
      ))}
    </ol>
  )
}
