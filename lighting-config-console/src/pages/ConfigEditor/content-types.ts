export const CONTENT_TYPE_VALUES = [
  'STRING',
  'BOOLEAN',
  'BYTE',
  'SHORT',
  'INTEGER',
  'LONG',
  'FLOAT',
  'DOUBLE',
  'LIST',
  'MAP',
] as const

export type ContentTypeValue = (typeof CONTENT_TYPE_VALUES)[number]

export const CONTENT_TYPE_OPTIONS: { value: ContentTypeValue; label: string }[] = [
  { value: 'STRING', label: '字符串 / String' },
  { value: 'BOOLEAN', label: '布尔型 / boolean' },
  { value: 'BYTE', label: 'Byte (整数)' },
  { value: 'SHORT', label: 'Short (整数)' },
  { value: 'INTEGER', label: '整数 / Integer' },
  { value: 'LONG', label: 'Long (整数)' },
  { value: 'FLOAT', label: 'Float (小数)' },
  { value: 'DOUBLE', label: 'Double (小数)' },
  { value: 'LIST', label: '列表 / JSON 数组' },
  { value: 'MAP', label: 'Map / JSON 对象' },
]
