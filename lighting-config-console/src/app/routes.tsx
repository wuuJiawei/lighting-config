import { createHashRouter, Navigate } from 'react-router-dom'
import { AppShell } from './layout'
import { DashboardPage } from '@/pages/Dashboard'
import { ConfigListPage } from '@/pages/ConfigList'
import { ConfigEditorPage } from '@/pages/ConfigEditor'
import { NamespacePage } from '@/pages/Namespace'
import { AuditPage } from '@/pages/Audit'
import { LoginPage } from '@/pages/Login'
import { RequireAuth } from './require-auth'

export const router = createHashRouter(
  [
    {
      path: '/login',
      element: <LoginPage />,
    },
    {
      path: '/',
      element: (
        <RequireAuth>
          <AppShell />
        </RequireAuth>
      ),
      children: [
        { index: true, element: <DashboardPage /> },
        { path: 'configs', element: <ConfigListPage /> },
        { path: 'configs/new', element: <ConfigEditorPage /> },
        { path: 'configs/:configId', element: <ConfigEditorPage /> },
        { path: 'namespaces', element: <NamespacePage /> },
        { path: 'audit', element: <AuditPage /> },
      ],
    },
    {
      path: '*',
      element: <Navigate to="/" replace />,
    },
  ],
)
