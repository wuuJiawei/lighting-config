import { useState } from 'react'
import type { ConfigItem } from '@/api/types'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/cn'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { formatDateTime } from '@/utils/date'
import { deleteConfig, rollbackConfig, upsertConfig } from '@/api/config'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'
import { ConfigDeleteDialog } from '@/components/shared/config-delete-dialog'
import { ConfigRollbackDialog } from '@/components/shared/config-rollback-dialog'

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
  const [deleteTarget, setDeleteTarget] = useState<ConfigItem | null>(null)
  const [rollbackTarget, setRollbackTarget] = useState<ConfigItem | null>(null)
  const skeletonRows = Array.from({ length: 5 }, (_, index) => index)
  const toggleMutation = useMutation({
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
  const deleteMutation = useMutation({
    mutationFn: (configId: string) => deleteConfig(configId),
    onSuccess: (_, configId) => {
      toast.success('配置已删除，客户端将回落到默认值')
      setDeleteTarget(null)
      void queryClient.invalidateQueries({ queryKey: ['configs'] })
      if (configId) {
        void queryClient.invalidateQueries({ queryKey: ['config', configId] })
        void queryClient.invalidateQueries({ queryKey: ['config-revisions', configId] })
      }
    },
    onError: () => toast.error('删除失败，请稍后再试'),
  })
  const rollbackMutation = useMutation({
    mutationFn: (payload: { configId: string; targetVersion: number }) => rollbackConfig(payload),
    onSuccess: (_, variables) => {
      toast.success(`已回滚到 v${variables.targetVersion}，客户端将自动同步`)
      setRollbackTarget(null)
      void queryClient.invalidateQueries({ queryKey: ['configs'] })
      if (variables.configId) {
        void queryClient.invalidateQueries({ queryKey: ['config', variables.configId] })
        void queryClient.invalidateQueries({ queryKey: ['config-revisions', variables.configId] })
      }
    },
    onError: () => toast.error('回滚失败，请稍后再试'),
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
                    <ToggleSwitch
                      checked={row.enabled}
                      disabled={toggleMutation.isPending}
                      onChange={() => toggleMutation.mutate(row)}
                    />
                  </div>
                </TableCell>
                <TableCell>v{row.version}</TableCell>
                <TableCell>{formatDateTime(row.updatedAt)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button size="sm" variant="ghost" onClick={() => void navigate(`/configs/${row.id}`, { state: listState })}>
                      编辑
                    </Button>
                    <Button size="sm" variant="ghost" onClick={() => setRollbackTarget(row)}>
                      回滚
                    </Button>
                    <Button
                      size="sm"
                      variant="ghost"
                      className="text-destructive hover:text-destructive"
                      onClick={() => setDeleteTarget(row)}
                    >
                      删除
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
      <ConfigRollbackDialog
        config={rollbackTarget}
        open={Boolean(rollbackTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setRollbackTarget(null)
          }
        }}
        isSubmitting={rollbackMutation.isPending}
        onConfirm={({ config, version }) => rollbackMutation.mutate({ configId: config.id, targetVersion: version })}
      />
      <ConfigDeleteDialog
        config={deleteTarget}
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(null)
          }
        }}
        isSubmitting={deleteMutation.isPending}
        onConfirm={(config) => deleteMutation.mutate(config.id)}
      />
    </div>
  )
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
