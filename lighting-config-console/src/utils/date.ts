export function formatDateTime(value: string | number | Date) {
  const date = new Date(value)
  return date.toLocaleString('zh-CN', {
    hour12: false,
  })
}

export function formatRelative(value: string | number | Date) {
  const date = new Date(value)
  const diff = Date.now() - date.getTime()
  if (diff < 60_000) return '刚刚'
  if (diff < 3_600_000) return `${Math.floor(diff / 60_000)} 分钟前`
  if (diff < 86_400_000) return `${Math.floor(diff / 3_600_000)} 小时前`
  return `${Math.floor(diff / 86_400_000)} 天前`
}
