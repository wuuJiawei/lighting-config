import type { ConfigStatus } from '@/api/types'
import { Badge } from '@/components/ui/badge'

const STATUS_MAP: Record<ConfigStatus, { label: string; variant: 'success' | 'warning' | 'destructive' | 'default' }> = {
  ACTIVE: { label: '生效', variant: 'success' },
  DISABLED: { label: '停用', variant: 'default' },
}

export function StatusBadge({ status }: { status: ConfigStatus }) {
  const meta = STATUS_MAP[status] ?? STATUS_MAP.ACTIVE
  return <Badge variant={meta.variant}>{meta.label}</Badge>
}
