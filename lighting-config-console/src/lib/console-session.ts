const SESSION_KEY = 'lighting-console-session'
const EDITOR_NAME_KEY = 'lighting-console-editor-name'

let cachedSessionId: string | null = null

function randomId() {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return Math.random().toString(16).slice(2) + Date.now().toString(16)
}

export function ensureConsoleSessionId(): string {
  if (cachedSessionId) {
    return cachedSessionId
  }
  if (typeof window !== 'undefined') {
    const existing = window.localStorage.getItem(SESSION_KEY)
    if (existing && existing.length > 0) {
      cachedSessionId = existing
      return existing
    }
    const generated = randomId()
    window.localStorage.setItem(SESSION_KEY, generated)
    cachedSessionId = generated
    return generated
  }
  cachedSessionId = randomId()
  return cachedSessionId
}

export function getEditorDisplayName(): string {
  if (typeof window === 'undefined') {
    return '控制台用户'
  }
  return window.localStorage.getItem(EDITOR_NAME_KEY) || '控制台用户'
}

export function setEditorDisplayName(name: string) {
  if (typeof window === 'undefined') {
    return
  }
  window.localStorage.setItem(EDITOR_NAME_KEY, name || '控制台用户')
}
