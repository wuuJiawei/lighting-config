import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/lighting-config/api',
  timeout: 15_000,
})

let authToken: string | null = null

export function setApiAuthToken(token: string | null) {
  authToken = token
}

apiClient.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      console.warn('未授权，请检查 token 或登录流程')
    }
    return Promise.reject(error instanceof Error ? error : new Error('Unknown API error'))
  },
)

export async function withApiFallback<T>(fn: () => Promise<T>, fallback: T, label: string): Promise<T> {
  try {
    return await fn()
  } catch (error) {
    console.warn(`[api:${label}] fallback payload`, error)
    return fallback
  }
}
