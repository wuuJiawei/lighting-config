import { useQuery } from '@tanstack/react-query'
import { fetchAuditTrail } from '@/api/audit'
import type { AuditRecord } from '@/api/types'
import { PageHeader } from '@/components/shared/page-header'
import { AuditTimeline } from '@/components/shared/audit-timeline'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export function AuditPage() {
  const { data = [] } = useQuery<AuditRecord[]>({
    queryKey: ['audit'],
    queryFn: () => fetchAuditTrail({ limit: 15 }),
  })

  return (
    <div className="space-y-6">
      <PageHeader title="审计日志" description="追踪发布、灰度、回滚的历史操作。" />
      <Card>
        <CardHeader>
          <CardTitle>最近操作</CardTitle>
        </CardHeader>
        <CardContent>
          <AuditTimeline records={data} />
        </CardContent>
      </Card>
    </div>
  )
}
