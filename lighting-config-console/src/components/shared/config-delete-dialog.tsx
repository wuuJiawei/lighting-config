import type { ConfigItem } from '@/api/types'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

interface ConfigDeleteDialogProps {
  config?: ConfigItem | null
  open: boolean
  onOpenChange: (open: boolean) => void
  onConfirm: (config: ConfigItem) => void
  isSubmitting?: boolean
}

export function ConfigDeleteDialog({ config, open, onConfirm, onOpenChange, isSubmitting }: ConfigDeleteDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>确认删除配置？</DialogTitle>
          <DialogDescription>
            删除后客户端会收到删除事件，使用代码里的默认值（若无则为 null）。该操作会记录 revision，确保可审计与回滚。
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2 rounded-md border bg-muted/40 p-4 text-sm">
          <div className="font-semibold">{config?.key}</div>
          <div className="text-muted-foreground">{config ? `${config.tenant} / ${config.namespace} / ${config.appId}` : '-'}</div>
          {config?.value ? <div className="truncate text-muted-foreground">当前值：{config.value}</div> : null}
        </div>
        <DialogFooter>
          <Button variant="ghost" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button
            variant="destructive"
            disabled={!config || isSubmitting}
            onClick={() => config && onConfirm(config)}
          >
            {isSubmitting ? '删除中...' : '确认删除'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
