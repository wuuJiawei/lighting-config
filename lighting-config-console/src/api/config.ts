import { apiClient, withApiFallback } from './client'
import { ADMIN_CONFIG_ENDPOINT, ADMIN_CONFIG_ROLLBACK, REVISIONS_ENDPOINT } from './routes'
import { mockConfigList, mockConfigs, mockRevisions } from './mocks'
import type { ConfigItem, ConfigListResponse, ConfigRevision, ConfigUpsertPayload } from './types'
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

interface ServerRevisionResponse {
  id?: string
  tenant?: string
  namespace?: string
  appId?: string
  key?: string
  version: number
  op: 'UPSERT' | 'DELETE'
  operator?: string
  diff?: string
  value?: string
  createdAt: string
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
  appId: '__global__',
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
      const { data } = await apiClient.get<ServerConfigResponse[]>(ADMIN_CONFIG_ENDPOINT, { params: query })
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
      const { data } = await apiClient.get<ServerConfigResponse[]>(ADMIN_CONFIG_ENDPOINT, {
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
      const { data } = await apiClient.post<ServerConfigResponse>(ADMIN_CONFIG_ENDPOINT, payload)
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

export async function deleteConfig(configId: string): Promise<void> {
  const coordinate = decodeConfigId(configId)
  return withApiFallback(
    async () => {
      await apiClient.delete<void>(ADMIN_CONFIG_ENDPOINT, { params: coordinate })
    },
    undefined,
    'config:delete',
  )
}

export async function fetchConfigRevisions(configId: string): Promise<ConfigRevision[]> {
  const coordinate = decodeConfigId(configId)
  return withApiFallback(
    async () => {
      const { data } = await apiClient.get<ServerRevisionResponse[]>(REVISIONS_ENDPOINT, { params: coordinate })
      return data.map(mapRevisionResponse).sort((a, b) => b.version - a.version)
    },
    mockRevisions,
    'config:revisions',
  )
}

export interface RollbackPayload {
  configId: string
  targetVersion: number
}

export async function rollbackConfig(payload: RollbackPayload): Promise<ConfigItem> {
  const coordinate = decodeConfigId(payload.configId)
  return withApiFallback(
    async () => {
      const { data } = await apiClient.post<ServerConfigResponse>(ADMIN_CONFIG_ROLLBACK, {
        ...coordinate,
        targetVersion: payload.targetVersion,
      })
      return mapConfigResponse(data)
    },
    mapConfigResponse({
      ...(mockConfigs.find((item) => item.id === payload.configId) ?? mockConfigs[0]),
      version: payload.targetVersion,
      updatedAt: new Date().toISOString(),
    }),
    'config:rollback',
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

function mapRevisionResponse(payload: ServerRevisionResponse): ConfigRevision {
  return {
    id: payload.id,
    tenant: payload.tenant,
    namespace: payload.namespace,
    appId: payload.appId,
    key: payload.key,
    version: payload.version,
    op: payload.op,
    operator: payload.operator,
    diff: payload.diff,
    value: payload.value,
    createdAt: payload.createdAt,
  }
}
