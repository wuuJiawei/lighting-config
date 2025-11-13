import { useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'

export interface ConfigFilters {
  tenant?: string
  namespace?: string
  appId?: string
  keyword?: string
}

const DEFAULT_FILTERS: Required<ConfigFilters> = {
  tenant: 'default',
  namespace: 'default',
  appId: 'default',
  keyword: '',
}

export function useConfigFilters() {
  const [searchParams, setSearchParams] = useSearchParams()

  const filters = useMemo<ConfigFilters>(() => {
    const entries = Object.fromEntries(searchParams.entries())
    return {
      tenant: entries.tenant ?? DEFAULT_FILTERS.tenant,
      namespace: entries.namespace ?? DEFAULT_FILTERS.namespace,
      appId: entries.appId ?? DEFAULT_FILTERS.appId,
      keyword: entries.keyword ?? DEFAULT_FILTERS.keyword,
    }
  }, [searchParams])

  const updateFilters = (next: ConfigFilters) => {
    const nextParams = new URLSearchParams()
    Object.entries({ ...filters, ...next }).forEach(([key, value]) => {
      const baseline = DEFAULT_FILTERS[key as keyof typeof DEFAULT_FILTERS]
      if (value && value !== baseline) {
        nextParams.set(key, value)
      }
    })
    setSearchParams(nextParams, { replace: true })
  }

  return { filters, updateFilters }
}
