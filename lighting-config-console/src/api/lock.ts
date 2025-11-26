import { apiClient, getApiAuthToken } from './client'
import { ADMIN_CONFIG_LOCKS, ADMIN_CONFIG_LOCKS_RELEASE, ADMIN_CONFIG_LOCKS_STREAM } from './routes'
import type { ConfigEditLockState, ConfigLockPayload } from './types'
import { ensureConsoleSessionId, getEditorDisplayName } from '@/lib/console-session'

export async function acquireConfigLock(payload: ConfigLockPayload): Promise<ConfigEditLockState> {
  const { data } = await apiClient.post<ConfigEditLockState>(ADMIN_CONFIG_LOCKS, payload)
  return data
}

export async function releaseConfigLock(payload: ConfigLockPayload): Promise<ConfigEditLockState> {
  const { data } = await apiClient.post<ConfigEditLockState>(ADMIN_CONFIG_LOCKS_RELEASE, payload)
  return data
}

export function buildLockStreamUrl(payload: ConfigLockPayload): string {
  const params = new URLSearchParams({
    tenant: payload.tenant,
    namespace: payload.namespace,
    appId: payload.appId,
    key: payload.key,
    sessionId: ensureConsoleSessionId(),
    editorName: payload.ownerName ?? getEditorDisplayName(),
  })
  const token = getApiAuthToken()
  if (token) {
    params.append('token', token)
  }
  const baseUrl = apiClient.defaults.baseURL ?? ''
  const prefix = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  return `${prefix}${ADMIN_CONFIG_LOCKS_STREAM}?${params.toString()}`
}
