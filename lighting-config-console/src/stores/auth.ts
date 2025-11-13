import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { setApiAuthToken } from '@/api/client'

interface AuthState {
  token: string | null
  login: (token: string) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist<AuthState>(
    (set) => ({
      token: null,
      login: (token: string) => {
        set({ token })
        setApiAuthToken(token)
      },
      logout: () => {
        set({ token: null })
        setApiAuthToken(null)
      },
    }),
    {
      name: 'lighting-auth',
      onRehydrateStorage: () => (state?: AuthState) => {
        if (state?.token) {
          setApiAuthToken(state.token)
        }
      },
    },
  ),
)
