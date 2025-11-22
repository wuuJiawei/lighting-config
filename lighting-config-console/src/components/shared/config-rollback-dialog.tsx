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
  const selectedRevision = useMemo(
    () => (selectedVersion ? revisions.find((rev) => rev.version === selectedVersion) ?? null : null),
    [revisions, selectedVersion],
  )
  const selectedRevisionValue = useMemo(() => (selectedRevision ? extractRevisionValue(selectedRevision) : null), [selectedRevision])

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
              <div className="space-y-2 rounded-md border p-3">
                {selectedRevision ? (
                  <>
                    <div className="flex items-start justify-between gap-3">
                      <div className="text-sm font-medium">
                        v{selectedRevision.version} · {selectedRevision.op === 'DELETE' ? '删除' : '发布'}
                        {selectedRevision.operator ? ` · ${selectedRevision.operator}` : ''}
                      </div>
                      <div className="text-xs text-muted-foreground">{formatDateTime(selectedRevision.createdAt)}</div>
                    </div>
                    <div className="rounded-md bg-muted/60 p-3">
                      <div className="mb-1 text-xs text-muted-foreground">版本内容</div>
                      <pre className="max-h-52 overflow-auto whitespace-pre-wrap break-words font-mono text-sm text-foreground">
                        {selectedRevisionValue ?? '（无内容）'}
                      </pre>
                    </div>
                  </>
                ) : (
                  <p className="text-sm text-muted-foreground">请选择要回滚的版本。</p>
                )}
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

function extractRevisionValue(revision: ConfigRevision): string | null {
  if (revision.value !== undefined && revision.value !== null) {
    return revision.value
  }
  if (!revision.diff) {
    return null
  }
  try {
    const parsed = JSON.parse(revision.diff) as Record<string, any>
    const afterValue = parsed?.after?.value
    const beforeValue = parsed?.before?.value
    if (afterValue !== undefined && afterValue !== null) {
      return String(afterValue)
    }
    if (beforeValue !== undefined && beforeValue !== null) {
      return String(beforeValue)
    }
  } catch {
    // ignore malformed diff content
  }
  return null
}
