import type { LucideIcon } from 'lucide-react'
import { LayoutDashboard, Layers, ListChecks, Shield } from 'lucide-react'

export interface NavItem {
  label: string
  path: string
  icon: LucideIcon
}

export const NAV_ITEMS: NavItem[] = [
  { label: '概览', path: '/', icon: LayoutDashboard },
  { label: '配置管理', path: '/configs', icon: ListChecks },
  { label: '命名空间', path: '/namespaces', icon: Layers },
  { label: '审计日志', path: '/audit', icon: Shield },
]
