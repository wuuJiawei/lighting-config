import type { DashboardStat } from '@/api/types'
import { ArrowDownRight, ArrowUpRight } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/cn'

interface StatCardProps {
  stat: DashboardStat
}

export function StatCard({ stat }: StatCardProps) {
  const TrendIcon = stat.trend === 'up' ? ArrowUpRight : ArrowDownRight
  const trendColor = stat.trend === 'up' ? 'text-green-600' : 'text-amber-600'

  return (
    <Card>
      <CardHeader className="space-y-1">
        <CardDescription>{stat.hint ?? '最近监控'}</CardDescription>
        <CardTitle>{stat.label}</CardTitle>
      </CardHeader>
      <CardContent className="flex items-end justify-between">
        <div className="text-3xl font-semibold">{stat.value}</div>
        <div className={cn('flex items-center text-sm font-medium', trendColor)}>
          <TrendIcon className="mr-1 h-4 w-4" />
          {stat.change}%
        </div>
      </CardContent>
    </Card>
  )
}
