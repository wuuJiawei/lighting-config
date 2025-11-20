import type { NamespaceSummary } from '@/api/types'
import type { ConfigFilters } from '@/hooks/useConfigFilters'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

interface ConfigFilterBarProps {
  filters: ConfigFilters
  namespaces: NamespaceSummary[]
  onChange: (next: ConfigFilters) => void
  isLoading?: boolean
}

const FALLBACK_NAMESPACE: NamespaceSummary = {
  id: 'default',
  name: 'default',
  owner: '-',
  configCount: 0,
  watchers: 0,
  appIds: ['__global__'],
  updatedAt: new Date().toISOString(),
}

export function ConfigFilterBar({ filters, namespaces, onChange, isLoading }: ConfigFilterBarProps) {
  const availableNamespaces = namespaces.length ? namespaces : [FALLBACK_NAMESPACE]
  const namespaceValue =
    filters.namespace && availableNamespaces.some((item) => item.name === filters.namespace)
      ? filters.namespace
      : availableNamespaces[0]?.name ?? FALLBACK_NAMESPACE.name
  const selectedNamespace =
    availableNamespaces.find((item) => item.name === namespaceValue) ?? availableNamespaces[0] ?? FALLBACK_NAMESPACE
  const appOptions = selectedNamespace.appIds.length ? selectedNamespace.appIds : ['__global__']
  const appValue = appOptions.includes(filters.appId ?? '') ? filters.appId ?? appOptions[0] : appOptions[0] ?? '__global__'

  const handleKeywordChange = (value: string) => {
    onChange({ ...filters, keyword: value })
  }

  const handleNamespaceChange = (value: string) => {
    const namespace = availableNamespaces.find((item) => item.name === value) ?? availableNamespaces[0] ?? FALLBACK_NAMESPACE
    const nextApp = namespace.appIds[0] ?? '__global__'
    onChange({
      ...filters,
      namespace: namespace.name,
      appId: nextApp,
    })
  }

  const handleAppChange = (value: string) => {
    onChange({ ...filters, appId: value })
  }

  const handleTenantChange = (value: string) => {
    onChange({ ...filters, tenant: value })
  }

  const resetFilters = () => {
    const firstNamespace = availableNamespaces[0] ?? FALLBACK_NAMESPACE
    onChange({
      tenant: 'default',
      namespace: firstNamespace.name,
      appId: firstNamespace.appIds[0] ?? '__global__',
      keyword: '',
    })
  }

  return (
    <div className="grid gap-4 rounded-xl border bg-card/60 p-4 md:grid-cols-4">
      <div className="space-y-2">
        <Label className="text-xs text-muted-foreground" htmlFor="keyword">
          搜索
        </Label>
        <Input
          id="keyword"
          placeholder="按 key / 描述过滤"
          value={filters.keyword ?? ''}
          onChange={(event) => handleKeywordChange(event.target.value)}
        />
      </div>
      <div className="space-y-2">
        <Label className="text-xs text-muted-foreground" htmlFor="namespace">
          命名空间
        </Label>
        <Select value={namespaceValue} onValueChange={handleNamespaceChange} disabled={isLoading}>
          <SelectTrigger id="namespace">
            <SelectValue placeholder="选择命名空间" />
          </SelectTrigger>
          <SelectContent>
            {availableNamespaces.map((ns) => (
              <SelectItem key={ns.id} value={ns.name}>
                {ns.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-2">
        <Label className="text-xs text-muted-foreground" htmlFor="appId">
          App ID
        </Label>
        <Select value={appValue} onValueChange={handleAppChange} disabled={isLoading}>
          <SelectTrigger id="appId">
            <SelectValue placeholder="选择 App ID" />
          </SelectTrigger>
          <SelectContent>
            {appOptions.map((app) => (
              <SelectItem key={app} value={app}>
                {app}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="space-y-2">
        <Label className="text-xs text-muted-foreground" htmlFor="tenant">
          租户
        </Label>
        <div className="flex gap-2">
          <Select value={filters.tenant ?? 'default'} onValueChange={handleTenantChange} disabled={isLoading}>
            <SelectTrigger id="tenant" className="flex-1">
              <SelectValue placeholder="选择租户" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="default">default</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="ghost" onClick={resetFilters} disabled={isLoading}>
            重置
          </Button>
        </div>
      </div>
    </div>
  )
}
