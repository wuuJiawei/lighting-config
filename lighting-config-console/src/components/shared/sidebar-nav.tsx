import type { NavItem } from '@/app/navigation'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/cn'
import { ChevronRight, X } from 'lucide-react'
import { NavLink } from 'react-router-dom'

interface SidebarNavProps {
  items: NavItem[]
  isOpen: boolean
  onClose: () => void
}

export function SidebarNav({ items, isOpen, onClose }: SidebarNavProps) {
  return (
    <>
      <div
        className={cn('fixed inset-0 z-30 bg-black/40 transition-opacity lg:hidden', isOpen ? 'opacity-100' : 'pointer-events-none opacity-0')}
        onClick={onClose}
      />
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-40 w-64 border-r bg-card p-5 transition-transform',
          isOpen ? 'translate-x-0' : '-translate-x-full',
          'lg:translate-x-0',
        )}
      >
        <div className="mb-8 flex items-center justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-muted-foreground">Lighting</p>
            <p className="text-lg font-bold">Config Center</p>
          </div>
          <Button variant="ghost" size="icon" className="lg:hidden" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>
        <nav className="space-y-1">
          {items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                cn(
                  'flex items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted/70 hover:text-foreground',
                  isActive && 'bg-muted text-foreground',
                )
              }
              onClick={onClose}
            >
              <span className="flex items-center gap-2">
                <item.icon className="h-4 w-4" />
                {item.label}
              </span>
              <ChevronRight className="h-4 w-4" />
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  )
}
