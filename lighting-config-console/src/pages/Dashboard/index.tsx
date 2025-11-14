import { useQuery } from '@tanstack/react-query'
import { fetchCacheMissAlerts, fetchDashboardStats, fetchRecentAudits } from '@/api/dashboard'
import { fetchConfigList } from '@/api/config'
import type { AuditRecord, CacheMissAlert, ConfigListResponse, DashboardStat } from '@/api/types'
import { PageHeader } from '@/components/shared/page-header'
import { StatCard } from '@/components/shared/stat-card'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { AuditTimeline } from '@/components/shared/audit-timeline'
import { CacheMissAlertList } from '@/components/shared/cache-miss-alerts'
import { formatRelative } from '@/utils/date'

export function DashboardPage() {
  const { data: stats = [], isLoading: statsLoading } = useQuery<DashboardStat[]>({
    queryKey: ['dashboard', 'stats'],
    queryFn: () => fetchDashboardStats(),
  })

  const { data: audits = [] } = useQuery<AuditRecord[]>({
    queryKey: ['dashboard', 'audits'],
    queryFn: () => fetchRecentAudits(5),
    staleTime: 60_000,
  })

  const { data: configs } = useQuery<ConfigListResponse>({
    queryKey: ['dashboard', 'configs'],
    queryFn: () => fetchConfigList(),
  })

  const { data: cacheMissAlerts = [] } = useQuery<CacheMissAlert[]>({
    queryKey: ['dashboard', 'cache-miss'],
    queryFn: () => fetchCacheMissAlerts(6),
    refetchInterval: 60_000,
  })

  const recentConfigs = configs?.items?.slice(0, 5) ?? []

  const placeholderStat: DashboardStat = { label: '加载中', value: '...', change: 0, trend: 'up' }
  const normalizedStats = Array.isArray(stats) ? stats : []
  const cardStats: DashboardStat[] = statsLoading
    ? Array.from({ length: 4 }, (_, index) => ({ ...placeholderStat, label: `加载中 ${index + 1}` }))
    : normalizedStats.length > 0
      ? normalizedStats
      : [placeholderStat]

  return (
    <div className="space-y-6">
      <PageHeader
        title="控制台概览"
        description="观察核心指标与推送状态，确保配置中心处于健康可控状态。"
      />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cardStats.map((stat, index) => (
          <StatCard key={`${stat.label}-${index}`} stat={stat} />
        ))}
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>最近发布</CardTitle>
            <CardDescription>按更新时间排序的最新配置</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {recentConfigs.map((item) => (
              <div key={item.id} className="flex items-center justify-between rounded-lg border px-4 py-3">
                <div>
                  <p className="font-medium">{item.key}</p>
                  <p className="text-xs text-muted-foreground">
                    {item.namespace} · {item.appId} · v{item.version}
                  </p>
                </div>
                <p className="text-xs text-muted-foreground">{formatRelative(item.updatedAt)}</p>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>审计快照</CardTitle>
            <CardDescription>追踪最近 24 小时的操作轨迹</CardDescription>
          </CardHeader>
          <CardContent>
            <AuditTimeline records={audits} />
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>缓存回源热点</CardTitle>
          <CardDescription>用于识别客户端轮询频繁穿透数据库的场景</CardDescription>
        </CardHeader>
        <CardContent>
          <CacheMissAlertList alerts={cacheMissAlerts} />
        </CardContent>
      </Card>
    </div>
  )
}
