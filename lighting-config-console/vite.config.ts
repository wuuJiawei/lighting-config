/// <reference types="vitest" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type { UserConfig } from 'vite'
import type { UserConfig as VitestUserConfig } from 'vitest/config'

const __dirname = fileURLToPath(new URL('.', import.meta.url))

const config: UserConfig & { test: VitestUserConfig['test'] } = {
  plugins: [react()],
  base: '/lighting-config/',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/lighting-config/api': {
        target: 'http://localhost:7086',
        changeOrigin: true,
      },
      '/actuator': {
        target: 'http://localhost:7086',
        changeOrigin: true,
      },
      // legacy aliases for earlier paths
      '/api': {
        target: 'http://localhost:7086/lighting-config/api',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/tests/setup.ts'],
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
    },
  },
}

export default defineConfig(config)
