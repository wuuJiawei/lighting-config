import type { NamespaceSummary } from '@/api/types'
import type { ConfigFilters } from '@/hooks/useConfigFilters'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/cn'

interface ConfigFilterBarProps {
  filters: ConfigFilters
  namespaces: NamespaceSummary[]
  onChange: (next: ConfigFilters) => void
  isLoading?: boolean
}

export function ConfigFilterBar({ filters, namespaces, onChange, isLoading }: ConfigFilterBarProps) {
  const handleInput = (key: keyof ConfigFilters, value: string) => {
    onChange({ ...filters, [key]: value })
  }

  const availableNamespaces = namespaces.length
    ? namespaces
    : [{ id: 'default', name: 'default', owner: '-', configCount: 0, watchers: 0, appIds: ['default'], updatedAt: new Date().toISOString() }]
  const selectedNamespace = availableNamespaces.find((item) => item.name === filters.namespace) ?? availableNamespaces[0]
  const appOptions = selectedNamespace.appIds.length ? selectedNamespace.appIds : ['default']

  const resetFilters = () =>
    onChange({
      tenant: 'default',
      namespace: availableNamespaces[0]?.name ?? 'default',
      appId: appOptions[0] ?? 'default',
      keyword: '',
    })

  return (
    <div className="grid gap-4 rounded-xl border bg-card/60 p-4 md:grid-cols-4">
      <div className="space-y-2">
        <label className="text-xs text-muted-foreground" htmlFor="keyword">
          搜索
        </label>
        <Input
          id="keyword"
          placeholder="按 key / 描述过滤"
          value={filters.keyword ?? ''}
          onChange={(event) => handleInput('keyword', event.target.value)}
        />
      </div>
      <div className="space-y-2">
        <label className="text-xs text-muted-foreground" htmlFor="namespace">
          命名空间
        </label>
        <select
          id="namespace"
          className={cn(
            'h-10 w-full rounded-md border border-input bg-background px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
          )}
          value={filters.namespace ?? selectedNamespace.name}
          onChange={(event) => handleInput('namespace', event.target.value)}
        >
          {availableNamespaces.map((ns) => (
            <option key={ns.id} value={ns.name}>
              {ns.name}
            </option>
          ))}
        </select>
      </div>
      <div className="space-y-2">
        <label className="text-xs text-muted-foreground" htmlFor="appId">
          App ID
        </label>
        <select
          id="appId"
          className="h-10 w-full rounded-md border border-input bg-background px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          value={filters.appId ?? appOptions[0]}
          onChange={(event) => handleInput('appId', event.target.value)}
        >
          {appOptions.map((app) => (
            <option key={app} value={app}>
              {app}
            </option>
          ))}
        </select>
      </div>
      <div className="space-y-2">
        <label className="text-xs text-muted-foreground" htmlFor="tenant">
          租户
        </label>
        <div className="flex gap-2">
          <select
            id="tenant"
            className="h-10 flex-1 rounded-md border border-input bg-background px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            value={filters.tenant ?? 'default'}
            onChange={(event) => handleInput('tenant', event.target.value)}
          >
            <option value="default">default</option>
          </select>
          <Button variant="ghost" onClick={resetFilters} disabled={isLoading}>
            重置
          </Button>
        </div>
      </div>
    </div>
  )
}
