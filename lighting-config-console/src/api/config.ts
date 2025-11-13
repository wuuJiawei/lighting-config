import { apiClient, withApiFallback } from './client'
import { mockConfigList, mockConfigs } from './mocks'
import type { ConfigItem, ConfigListResponse, ConfigUpsertPayload } from './types'
import { decodeConfigId, deriveConfigId } from '@/utils/config-id'

interface ServerConfigResponse {
  id: string
  tenant: string
  namespace: string
  appId: string
  key: string
  value: string
  contentType: string
  labels: Record<string, string>
  enabled: boolean
  version: number
  updatedAt: string
}

export interface ConfigQueryParams {
  tenant?: string
  namespace?: string
  appId?: string
  keyword?: string
}

const DEFAULT_QUERY: Required<ConfigQueryParams> = {
  tenant: 'default',
  namespace: 'default',
  appId: 'default',
  keyword: '',
}

export async function fetchConfigList(params: ConfigQueryParams = {}): Promise<ConfigListResponse> {
  const merged = { ...DEFAULT_QUERY, ...params }
  const query = {
    tenant: merged.tenant,
    namespace: merged.namespace,
    appId: merged.appId,
    prefix: merged.keyword?.trim() || undefined,
  }

  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<ServerConfigResponse[]>('/config', { params: query })
      return {
        items: data.map(mapConfigResponse),
        total: data.length,
      }
    },
    mockConfigList,
    'config:get',
  )
}

export async function fetchConfigDetail(configId: string): Promise<ConfigItem> {
  const coordinate = decodeConfigId(configId)
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<ServerConfigResponse[]>('/config', {
        params: {
          tenant: coordinate.tenant,
          namespace: coordinate.namespace,
          appId: coordinate.appId,
          key: coordinate.key,
        },
      })
      const matched = data[0]
      if (!matched) {
        throw new Error('配置不存在')
      }
      return mapConfigResponse(matched)
    },
    mockConfigs.find((item) => item.id === configId) ?? mockConfigs[0],
    'config:getDetail',
  )
}

export async function upsertConfig(payload: ConfigUpsertPayload): Promise<ConfigItem> {
  return withApiFallback(
    async () => {
      const { data } = await apiClient.post<ServerConfigResponse>('/config', payload)
      return mapConfigResponse(data)
    },
    mapConfigResponse({
      id: deriveConfigId(payload),
      tenant: payload.tenant,
      namespace: payload.namespace,
      appId: payload.appId,
      key: payload.key,
      value: payload.value,
      contentType: payload.contentType,
      labels: payload.labels,
      enabled: payload.enabled,
      version: 1,
      updatedAt: new Date().toISOString(),
    }),
    'config:upsert',
  )
}

function mapConfigResponse(payload: ServerConfigResponse): ConfigItem {
  return {
    id: payload.id ?? deriveConfigId(payload),
    tenant: payload.tenant,
    namespace: payload.namespace,
    appId: payload.appId,
    key: payload.key,
    value: payload.value,
    contentType: payload.contentType,
    labels: payload.labels ?? {},
    enabled: payload.enabled,
    version: payload.version,
    updatedAt: payload.updatedAt,
  }
}
