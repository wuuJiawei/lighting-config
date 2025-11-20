import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { NAV_ITEMS } from './navigation'
import { SidebarNav } from '@/components/shared/sidebar-nav'
import { TopNav } from '@/components/shared/top-nav'

export function AppShell() {
  const [isOpen, setIsOpen] = useState(false)

  return (
    <div className="min-h-screen bg-background lg:pl-64">
      <SidebarNav items={NAV_ITEMS} isOpen={isOpen} onClose={() => setIsOpen(false)} />
      <div className="flex min-h-screen flex-col">
        <TopNav onMenuClick={() => setIsOpen(true)} />
        <main className="flex-1 space-y-6 bg-muted/20 px-6 py-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
