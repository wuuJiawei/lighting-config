import { cpSync, existsSync, mkdirSync, rmSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const projectRoot = resolve(__dirname, '..')
const distDir = resolve(projectRoot, 'dist')
const serverStaticRoot = resolve(
  projectRoot,
  '..',
  'lighting-config-server',
  'src',
  'main',
  'resources',
  'static',
)
const serverConsoleDir = resolve(serverStaticRoot, 'lighting-config')

if (!existsSync(distDir)) {
  console.warn('[sync:server] dist folder not found, skip syncing. Run "pnpm build" first.')
  process.exit(0)
}

mkdirSync(serverStaticRoot, { recursive: true })
rmSync(serverConsoleDir, { recursive: true, force: true })
cpSync(distDir, serverConsoleDir, { recursive: true })

console.log(`Copied console build to ${serverConsoleDir}`)
