import { useEffect, useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import type { ConfigItem, ConfigRevision } from '@/api/types'
import { fetchConfigRevisions } from '@/api/config'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatDateTime } from '@/utils/date'

interface ConfigRollbackDialogProps {
  config?: ConfigItem | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onConfirm: (input: { config: ConfigItem; version: number }) => void
  isSubmitting?: boolean
}

export function ConfigRollbackDialog({ config, open, onOpenChange, onConfirm, isSubmitting }: ConfigRollbackDialogProps) {
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null)
  const revisionsQuery = useQuery<ConfigRevision[]>({
    queryKey: ['config-revisions', config?.id],
    queryFn: () => fetchConfigRevisions(config?.id ?? ''),
    enabled: open && Boolean(config?.id),
  })

  const revisions = useMemo(() => revisionsQuery.data ?? [], [revisionsQuery.data])

  useEffect(() => {
    if (!open) {
      setSelectedVersion(null)
      return
    }
    if (revisions.length === 0) {
      setSelectedVersion(null)
      return
    }
    const fallbackVersion = revisions.find((rev) => rev.version !== config?.version) ?? revisions[0]
    setSelectedVersion(fallbackVersion.version)
  }, [open, revisions, config?.version])

  const versionOptions = useMemo(() => {
    const filtered = revisions.filter((rev) => rev.version !== config?.version)
    const base = filtered.length ? filtered : revisions
    return base.map((rev) => ({ label: `v${rev.version}`, value: rev.version }))
  }, [config?.version, revisions])

  const currentVersion = config?.version ?? null
  const isBroken = revisionsQuery.isError
  const hasHistory = revisions.length > 0

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>回滚配置</DialogTitle>
          <DialogDescription>
            选择一个历史版本进行回滚。保存后服务端会写入新的 revision 并通知客户端自动拉取回滚后的值。
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3">
          <div className="text-sm">
            <div className="font-semibold">{config?.key}</div>
            <div className="text-muted-foreground">{config ? `${config.tenant} / ${config.namespace} / ${config.appId}` : '-'}</div>
            {currentVersion ? <div className="text-xs text-muted-foreground">当前版本 v{currentVersion}</div> : null}
          </div>

          {revisionsQuery.isLoading ? (
            <div className="h-10 animate-pulse rounded-md bg-muted" />
          ) : isBroken ? (
            <p className="text-sm text-destructive">加载历史版本失败，请稍后重试。</p>
          ) : !hasHistory ? (
            <p className="text-sm text-muted-foreground">暂无历史版本可回滚。</p>
          ) : (
            <>
              <Select
                value={selectedVersion ? String(selectedVersion) : undefined}
                onValueChange={(value) => setSelectedVersion(Number(value))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="选择目标版本" />
                </SelectTrigger>
                <SelectContent>
                  {versionOptions.map((option) => (
                    <SelectItem key={option.value} value={String(option.value)}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="max-h-52 space-y-2 overflow-y-auto rounded-md border p-2">
                {revisions.map((rev) => (
                  <div
                    key={rev.id ?? `${rev.version}-${rev.createdAt}`}
                    className="flex items-start justify-between rounded-md border border-transparent p-2 hover:border-muted"
                  >
                    <div>
                      <div className="text-sm font-medium">
                        v{rev.version} · {rev.op === 'DELETE' ? '删除' : '发布'}
                        {rev.operator ? ` · ${rev.operator}` : ''}
                      </div>
                      <div className="text-xs text-muted-foreground">{formatDateTime(rev.createdAt)}</div>
                    </div>
                    {rev.diff ? <div className="max-w-[180px] text-right text-xs text-muted-foreground">{rev.diff}</div> : null}
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button
            disabled={!config || !selectedVersion || revisionsQuery.isLoading || isSubmitting}
            onClick={() => config && selectedVersion && onConfirm({ config, version: selectedVersion })}
          >
            {isSubmitting ? '回滚中...' : '确认回滚'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
