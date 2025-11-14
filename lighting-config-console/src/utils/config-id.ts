export interface ConfigCoordinate {
  tenant: string
  namespace: string
  appId: string
  key: string
}

const DELIMITER = ':'

export function encodeConfigId(coordinate: ConfigCoordinate): string {
  return [coordinate.tenant, coordinate.namespace, coordinate.appId, coordinate.key]
    .map((part) => encodeURIComponent(part))
    .join(DELIMITER)
}

export function decodeConfigId(id: string): ConfigCoordinate {
  const rawParts = id.split(DELIMITER)
  const parts =
    rawParts.length === 4
      ? rawParts
      : id.split('::') // backward compatibility with old IDs
  if (parts.length !== 4) {
    throw new Error(`非法配置 ID: ${id}`)
  }
  const [tenant, namespace, appId, key] = parts.map((part) => decodeURIComponent(part))
  return { tenant, namespace, appId, key }
}

export function deriveConfigId(coordinate: ConfigCoordinate): string {
  return encodeConfigId(coordinate)
}
