import type { ConfigItem, ConfigStatus } from '@/api/types'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { StatusBadge } from '@/components/shared/status-badge'
import { Button } from '@/components/ui/button'
import { formatDateTime } from '@/utils/date'
import { useNavigate } from 'react-router-dom'

interface ConfigTableProps {
  items: ConfigItem[]
  isLoading?: boolean
  listState?: {
    from: {
      pathname: string
      search?: string
    }
  }
}

export function ConfigTable({ items, isLoading, listState }: ConfigTableProps) {
  const navigate = useNavigate()
  const skeletonRows = Array.from({ length: 5 }, (_, index) => index)

  if (!items.length && !isLoading) {
    return (
      <div className="flex h-48 flex-col items-center justify-center rounded-xl border text-center text-muted-foreground">
        暂无配置，点击右上角“新建配置”开始吧。
      </div>
    )
  }

  const rows: (ConfigItem | number)[] = isLoading ? skeletonRows : items

  return (
    <div className="overflow-hidden rounded-xl border bg-card/70">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Key</TableHead>
            <TableHead>命名空间</TableHead>
            <TableHead>App ID</TableHead>
            <TableHead>状态</TableHead>
            <TableHead>版本</TableHead>
            <TableHead>更新时间</TableHead>
            <TableHead className="text-right">操作</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((row) => {
            if (typeof row === 'number') {
              return (
                <TableRow key={`skeleton-${row}`}>
                  {Array.from({ length: 7 }).map((_, index) => (
                    <TableCell key={`cell-${index}`}>
                      <div className="h-4 animate-pulse rounded bg-muted" />
                    </TableCell>
                  ))}
                </TableRow>
              )
            }

            return (
              <TableRow key={row.id}>
                <TableCell className="font-medium">{row.key}</TableCell>
                <TableCell>{row.namespace}</TableCell>
                <TableCell>{row.appId}</TableCell>
                <TableCell>
                  <StatusBadge status={toStatus(row)} />
                </TableCell>
                <TableCell>v{row.version}</TableCell>
                <TableCell>{formatDateTime(row.updatedAt)}</TableCell>
                <TableCell className="text-right">
                  <Button
                    size="sm"
                    variant="ghost"
                    onClick={() => void navigate(`/configs/${row.id}`, { state: listState })}
                  >
                    编辑
                  </Button>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}

function toStatus(item: ConfigItem): ConfigStatus {
  return item.enabled ? 'ACTIVE' : 'DISABLED'
}
