import { describe, expect, it, vi } from 'vitest'
import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { DashboardPage } from '@/pages/Dashboard'

const queryClient = new QueryClient()

vi.mock('@/api/dashboard', () => ({
  fetchDashboardStats: () => Promise.resolve([]),
  fetchRecentAudits: () => Promise.resolve([]),
}))

vi.mock('@/api/config', () => ({
  fetchConfigList: () => Promise.resolve({ items: [], total: 0 }),
}))

describe('DashboardPage', () => {
  it('renders stat cards placeholder', () => {
    const { getByText } = render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter>
          <DashboardPage />
        </MemoryRouter>
      </QueryClientProvider>,
    )

    expect(getByText('控制台概览')).toBeInTheDocument()
  })
})
