import { apiClient } from './client'

export interface LoginRequestPayload {
  token: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export async function login(payload: LoginRequestPayload): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/auth/login', payload)
  return data
}
