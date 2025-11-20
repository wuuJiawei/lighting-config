import type { ConfigItem, ConfigStatus } from '@/api/types'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { StatusBadge } from '@/components/shared/status-badge'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/cn'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { formatDateTime } from '@/utils/date'
import { upsertConfig } from '@/api/config'
import { toast } from 'sonner'
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
  const queryClient = useQueryClient()
  const skeletonRows = Array.from({ length: 5 }, (_, index) => index)
  const mutation = useMutation({
    mutationFn: (item: ConfigItem) =>
      upsertConfig({
        tenant: item.tenant,
        namespace: item.namespace,
        appId: item.appId,
        key: item.key,
        value: item.value,
        contentType: item.contentType,
        labels: item.labels,
        enabled: !item.enabled,
      }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['configs'] })
      toast.success('已更新配置状态')
    },
    onError: () => toast.error('更新状态失败'),
  })

  if (!items.length && !isLoading) {
    return (
      <div className="flex h-48 flex-col items-center justify-center rounded-xl border text-center text-muted-foreground">
        暂无配置，点击右上角“新建配置”开始吧。
      </div>
    )
  }

  const rows: (ConfigItem | number)[] = isLoading ? skeletonRows : items

  return (
    <div className="relative overflow-x-auto rounded-xl border bg-card/70">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Key</TableHead>
            <TableHead className="w-[220px]">值预览</TableHead>
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
                  {Array.from({ length: 8 }).map((_, index) => (
                    <TableCell key={`cell-${index}`}>
                      <div className="h-4 animate-pulse rounded bg-muted" />
                    </TableCell>
                  ))}
                </TableRow>
              )
            }

            return (
              <TableRow key={row.id}>
                <TableCell className="max-w-[200px] truncate font-medium" title={row.key}>
                  {row.key}
                </TableCell>
                <TableCell className="max-w-[220px]">
                  <ValuePreview value={row.value} />
                </TableCell>
                <TableCell>{row.namespace}</TableCell>
                <TableCell>{row.appId}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <ToggleSwitch checked={row.enabled} disabled={mutation.isPending} onChange={() => mutation.mutate(row)} />
                  </div>
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

interface ToggleSwitchProps {
  checked: boolean
  disabled?: boolean
  onChange: () => void
}

function ToggleSwitch({ checked, disabled, onChange }: ToggleSwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      className={cn(
        'relative inline-flex h-6 w-12 items-center rounded-full border border-muted-foreground/40 bg-muted transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
        checked && 'bg-emerald-500/80 text-white',
        disabled && 'cursor-not-allowed opacity-60',
      )}
      onClick={onChange}
      disabled={disabled}
      aria-label={checked ? '点击禁用' : '点击启用'}
      title={checked ? '点击禁用' : '点击启用'}
    >
      <span
        className={cn(
          'absolute left-1 h-4 w-4 rounded-full bg-background shadow transition-transform',
          checked && 'translate-x-6 bg-white',
        )}
      />
    </button>
  )
}

function ValuePreview({ value }: { value: string }) {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <span
            className="block w-full cursor-default overflow-hidden text-ellipsis whitespace-nowrap text-muted-foreground"
            aria-label={value}
          >
            {value}
          </span>
        </TooltipTrigger>
        <TooltipContent className="max-w-[360px] whitespace-pre-wrap">
          {value}
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  )
}
