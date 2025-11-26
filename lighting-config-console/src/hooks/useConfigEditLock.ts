import axios from 'axios'
import { useEffect, useMemo, useRef, useState, useCallback } from 'react'
import { acquireConfigLock, buildLockStreamUrl, releaseConfigLock } from '@/api/lock'
import type { ConfigEditLockState, ConfigLockPayload } from '@/api/types'
import { getEditorDisplayName } from '@/lib/console-session'

function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (typeof error.response?.data === 'string') {
      return error.response.data
    }
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return '获取编辑锁失败，请稍后重试'
}

export function useConfigEditLock(payload?: ConfigLockPayload, enabled = true) {
  const [lockState, setLockState] = useState<ConfigEditLockState | null>(null)
  const [acquiring, setAcquiring] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const eventSourceRef = useRef<EventSource | null>(null)
  const ownedRef = useRef(false)

  const coordinateKey = useMemo(() => {
    if (!payload) {
      return ''
    }
    return `${payload.tenant}::${payload.namespace}::${payload.appId}::${payload.key}`
  }, [payload])

  useEffect(() => {
    if (!enabled || !payload) {
      return undefined
    }
    let cancelled = false

    const connectSse = () => {
      const url = buildLockStreamUrl({ ...payload, ownerName: payload.ownerName ?? getEditorDisplayName() })
      const es = new EventSource(url)
      eventSourceRef.current = es
      const handler = (event: MessageEvent) => {
        try {
          const next: ConfigEditLockState = JSON.parse(event.data)
          setLockState(next)
          ownedRef.current = next.locked && next.ownedByMe
          setError(null)
        } catch (parseError) {
          setError('编辑锁事件解析失败')
        }
      }
      es.addEventListener('lock', handler as EventListener)
      es.onmessage = handler
      es.onerror = () => {
        setError('编辑锁事件流中断，正在尝试重连...')
      }
    }

    const acquire = async () => {
      setAcquiring(true)
      setError(null)
      try {
        const result = await acquireConfigLock({ ...payload, ownerName: payload.ownerName ?? getEditorDisplayName() })
        if (!cancelled) {
          setLockState(result)
          ownedRef.current = result.locked && result.ownedByMe
        }
      } catch (acquireError) {
        if (!cancelled) {
          setError(getErrorMessage(acquireError))
        }
      } finally {
        if (!cancelled) {
          setAcquiring(false)
        }
      }
    }

    connectSse()
    void acquire()

    return () => {
      cancelled = true
      eventSourceRef.current?.close()
      eventSourceRef.current = null
      if (ownedRef.current) {
        void releaseConfigLock({ ...payload, ownerName: payload.ownerName ?? getEditorDisplayName() }).catch(() => undefined)
      }
      ownedRef.current = false
    }
  }, [coordinateKey, enabled, payload])

  const release = useCallback(async () => {
    if (!payload) {
      return
    }
    try {
      const result = await releaseConfigLock({ ...payload, ownerName: payload.ownerName ?? getEditorDisplayName() })
      setLockState(result)
      ownedRef.current = false
    } catch (releaseError) {
      setError(getErrorMessage(releaseError))
    }
  }, [payload])

  return {
    lockState,
    acquiring,
    error,
    release,
  }
}
