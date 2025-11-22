import { useEffect, useState, type ReactNode } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm, useWatch, type SubmitHandler } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { deleteConfig, fetchConfigDetail, rollbackConfig, upsertConfig } from '@/api/config'
import type { ConfigItem } from '@/api/types'
import { fetchNamespaces } from '@/api/namespace'
import { PageHeader } from '@/components/shared/page-header'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { CreatableSelect } from '@/components/ui/creatable-select'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useConfigEditorStore } from '@/stores/config-editor'
import { toast } from 'sonner'
import { ValueEditor } from './value-editor'
import { CONTENT_TYPE_OPTIONS, CONTENT_TYPE_VALUES, type ContentTypeValue } from './content-types'
import { ConfigDeleteDialog } from '@/components/shared/config-delete-dialog'
import { ConfigRollbackDialog } from '@/components/shared/config-rollback-dialog'

const formSchema = z
  .object({
    tenant: z.string().min(1, '租户必填'),
    namespace: z.string().min(1, '命名空间必填'),
    appId: z.string().min(1, 'App ID 必填'),
    key: z.string().min(1, 'Key 必填'),
    value: z.string().min(1, '配置内容不能为空'),
    contentType: z.enum(CONTENT_TYPE_VALUES),
    enabled: z.boolean(),
  })
  .superRefine((values, ctx) => {
    const error = validateValueForContentType(values.value, values.contentType)
    if (error) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['value'],
        message: error,
      })
    }
  })

type FormValues = z.infer<typeof formSchema>

const DEFAULT_VALUES: FormValues = {
  tenant: 'default',
  namespace: 'default',
  appId: '__global__',
  key: '',
  value: '',
  contentType: 'STRING',
  enabled: true,
}

export function ConfigEditorPage() {
  const params = useParams<{ configId?: string }>()
  const configId = params.configId
  const isNew = !configId || configId === 'new'
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const returnTo = (location.state as { from?: { pathname: string; search?: string } } | undefined)?.from ?? {
    pathname: '/configs',
    search: '',
  }
  const { draft, setDraft, updateContent } = useConfigEditorStore()
  const [deleteTarget, setDeleteTarget] = useState<ConfigItem | undefined>(undefined)
  const [rollbackTarget, setRollbackTarget] = useState<ConfigItem | undefined>(undefined)

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: DEFAULT_VALUES,
  })

  const enabledValue =
    useWatch<FormValues, 'enabled'>({
      control: form.control,
      name: 'enabled',
    }) ?? true
  const tenantValue =
    useWatch<FormValues, 'tenant'>({
      control: form.control,
      name: 'tenant',
    }) ?? DEFAULT_VALUES.tenant
  const namespaceValue =
    useWatch<FormValues, 'namespace'>({
      control: form.control,
      name: 'namespace',
    }) ?? DEFAULT_VALUES.namespace
  const appIdValue =
    useWatch<FormValues, 'appId'>({
      control: form.control,
      name: 'appId',
    }) ?? DEFAULT_VALUES.appId
  const contentTypeValue =
    useWatch<FormValues, 'contentType'>({
      control: form.control,
      name: 'contentType',
    }) ?? ('STRING' as ContentTypeValue)
  const value =
    useWatch<FormValues, 'value'>({
      control: form.control,
      name: 'value',
    }) ?? ''

  const handleContentTypeChange = (nextType: ContentTypeValue) => {
    const previousValue = form.getValues('value')
    const normalizedValue = normalizeValueForContentType(previousValue, nextType)
    form.setValue('contentType', nextType, { shouldDirty: true })
    form.setValue('value', normalizedValue, { shouldDirty: true, shouldValidate: true })
    updateContent(normalizedValue)
  }

  const handleValueChange = (nextValue: string) => {
    form.setValue('value', nextValue, { shouldDirty: true, shouldValidate: true })
    updateContent(nextValue)
  }

  const detailQuery = useQuery({
    queryKey: ['config', configId],
    queryFn: () => fetchConfigDetail(configId!),
    enabled: Boolean(configId) && !isNew,
  })
  const namespacesQuery = useQuery({
    queryKey: ['namespaces', tenantValue],
    queryFn: () => fetchNamespaces(tenantValue),
  })
  const loadedConfig = detailQuery.data && detailQuery.data.id === configId ? detailQuery.data : undefined

  useEffect(() => {
    if (isNew) {
      setDraft(undefined)
      form.reset(DEFAULT_VALUES)
      updateContent(DEFAULT_VALUES.value)
      return
    }
    if (!configId) {
      return
    }
    if (!loadedConfig) {
      setDraft(undefined)
      form.reset(DEFAULT_VALUES)
      updateContent(DEFAULT_VALUES.value)
    }
  }, [configId, form, isNew, loadedConfig, setDraft, updateContent])

  useEffect(() => {
    if (loadedConfig) {
      const { tenant, namespace, appId, key, value, contentType, enabled } = loadedConfig
      const safeContentType: ContentTypeValue = isContentTypeValue(contentType) ? contentType : 'STRING'
      const normalizedValue = normalizeValueForContentType(value, safeContentType)
      setDraft({
        ...loadedConfig,
        value: normalizedValue,
      })
      updateContent(normalizedValue)
      form.reset({
        tenant,
        namespace,
        appId,
        key,
        value: normalizedValue,
        contentType: safeContentType,
        enabled,
      })
    }
  }, [form, loadedConfig, setDraft, updateContent])

  const saveMutation = useMutation({
    mutationFn: (values: FormValues) =>
      upsertConfig({
        tenant: values.tenant,
        namespace: values.namespace,
        appId: values.appId,
        key: values.key,
        value: values.value,
        contentType: values.contentType,
        labels: draft?.labels ?? {},
        enabled: values.enabled,
      }),
    onSuccess: (result) => {
      toast.success('配置已保存')
      setDraft(result)
      queryClient.setQueryData(['config', result.id], result)
      void navigate(`/configs/${result.id}`, { replace: true, state: location.state })
    },
    onError: () => toast.error('保存失败，请稍后再试'),
  })

  const deleteMutation = useMutation({
    mutationFn: (targetId: string) => deleteConfig(targetId),
    onSuccess: (_, targetId) => {
      toast.success('配置已删除，客户端将使用默认值')
      setDeleteTarget(undefined)
      void queryClient.invalidateQueries({ queryKey: ['configs'] })
      if (targetId) {
        void queryClient.invalidateQueries({ queryKey: ['config', targetId] })
        void queryClient.invalidateQueries({ queryKey: ['config-revisions', targetId] })
      }
      void navigate({
        pathname: returnTo.pathname,
        search: returnTo.search ?? '',
      })
    },
    onError: () => toast.error('删除失败，请稍后再试'),
  })

  const rollbackMutation = useMutation({
    mutationFn: (payload: { configId: string; targetVersion: number }) => rollbackConfig(payload),
    onSuccess: (result, variables) => {
      toast.success(`已回滚到 v${variables.targetVersion}，客户端将自动同步`)
      setRollbackTarget(undefined)
      void queryClient.invalidateQueries({ queryKey: ['configs'] })
      if (configId) {
        queryClient.setQueryData(['config', configId], result)
        void queryClient.invalidateQueries({ queryKey: ['config-revisions', configId] })
      }
    },
    onError: () => toast.error('回滚失败，请稍后再试'),
  })

  const onSubmit: SubmitHandler<FormValues> = (values) => {
    if (!isNew && loadedConfig && isSameAsExisting(values, loadedConfig, draft?.labels)) {
      toast.info('未检测到变更，已跳过保存')
      return
    }
    saveMutation.mutate(values)
  }

  const availableNamespaces = namespacesQuery.data ?? []
  const namespaceOptions = toUniqueOptions([
    namespaceValue,
    DEFAULT_VALUES.namespace,
    ...availableNamespaces.map((item) => item.name),
  ])
  const selectedNamespace = availableNamespaces.find((item) => item.name === namespaceValue)
  const appOptions = toUniqueOptions([
    appIdValue,
    DEFAULT_VALUES.appId,
    ...(selectedNamespace?.appIds ?? []),
  ])
  const tenantOptions = toUniqueOptions([tenantValue, DEFAULT_VALUES.tenant])

  return (
    <div className="space-y-6">
      <PageHeader
        title={isNew ? '新建配置' : `编辑配置 · ${draft?.key ?? configId}`}
        description={isNew ? '按照租户 / 命名空间 / App ID 填充配置内容。' : '在保存前确认启用状态与内容格式。'}
        actions={
          <>
            {!isNew ? (
              <>
                <Button variant="ghost" disabled={!loadedConfig} onClick={() => loadedConfig && setRollbackTarget(loadedConfig)}>
                  回滚
                </Button>
                <Button
                  variant="destructive"
                  disabled={!loadedConfig}
                  onClick={() => loadedConfig && setDeleteTarget(loadedConfig)}
                >
                  删除
                </Button>
              </>
            ) : null}
            <Button
              variant="secondary"
              onClick={() =>
                void navigate({
                  pathname: returnTo.pathname,
                  search: returnTo.search ?? '',
                })
              }
            >
              返回列表
            </Button>
          </>
        }
      />

      <form onSubmit={(event) => void form.handleSubmit(onSubmit)(event)} className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>基础信息</CardTitle>
            <CardDescription>定义 tenant / namespace / appId / key</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="租户" error={form.formState.errors.tenant?.message}>
                <CreatableSelect
                  value={tenantValue}
                  options={tenantOptions}
                  placeholder="选择或创建租户"
                  inputPlaceholder="输入租户名称，回车快速创建"
                  onChange={(next) => form.setValue('tenant', next, { shouldDirty: true, shouldValidate: true })}
                />
              </Field>
              <Field label="命名空间" error={form.formState.errors.namespace?.message}>
                <CreatableSelect
                  value={namespaceValue}
                  options={namespaceOptions}
                  placeholder="选择或创建命名空间"
                  inputPlaceholder="输入命名空间，回车快速创建"
                  onChange={(next) => form.setValue('namespace', next, { shouldDirty: true, shouldValidate: true })}
                />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="App ID" error={form.formState.errors.appId?.message}>
                <CreatableSelect
                  value={appIdValue}
                  options={appOptions}
                  placeholder="选择或创建 App ID"
                  inputPlaceholder="输入 App ID，回车快速创建"
                  onChange={(next) => form.setValue('appId', next, { shouldDirty: true, shouldValidate: true })}
                />
              </Field>
              <Field label="Key" error={form.formState.errors.key?.message}>
                <Input {...form.register('key')} placeholder="config.example" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="格式">
                <Select value={contentTypeValue} onValueChange={(value: ContentTypeValue) => handleContentTypeChange(value)}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择格式" />
                  </SelectTrigger>
                  <SelectContent>
                    {CONTENT_TYPE_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </Field>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>发布策略</CardTitle>
            <CardDescription>控制配置启用或禁用</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                className="h-4 w-4"
                checked={enabledValue}
                onChange={(event) => form.setValue('enabled', event.target.checked)}
              />
              启用配置
            </label>
            <Button type="submit" disabled={saveMutation.isPending} className="w-full">
              {saveMutation.isPending ? '保存中...' : '保存配置'}
            </Button>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>配置内容</CardTitle>
            <CardDescription>根据内容类型提供 Switch / 数字输入 / JSON 编辑器，默认使用简单字符串</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <ValueEditor contentType={contentTypeValue} value={value} onChange={handleValueChange} />
            {form.formState.errors.value ? (
              <p className="text-sm text-destructive">{form.formState.errors.value.message}</p>
            ) : null}
          </CardContent>
        </Card>
      </form>
      <ConfigRollbackDialog
        config={rollbackTarget}
        open={Boolean(rollbackTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setRollbackTarget(undefined)
          }
        }}
        isSubmitting={rollbackMutation.isPending}
        onConfirm={({ config, version }) => rollbackMutation.mutate({ configId: config.id, targetVersion: version })}
      />
      <ConfigDeleteDialog
        config={deleteTarget}
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open) {
            setDeleteTarget(undefined)
          }
        }}
        isSubmitting={deleteMutation.isPending}
        onConfirm={(config) => deleteMutation.mutate(config.id)}
      />
    </div>
  )
}

interface FieldProps {
  label: string
  children: ReactNode
  error?: string
}

function Field({ label, children, error }: FieldProps) {
  return (
    <div className="space-y-2 text-sm">
      <Label>{label}</Label>
      {children}
      {error ? <p className="text-xs text-destructive">{error}</p> : null}
    </div>
  )
}

function toUniqueOptions(values: Array<string | undefined>) {
  const seen = new Set<string>()
  return values
    .filter((value): value is string => Boolean(value))
    .filter((value) => {
      if (seen.has(value)) {
        return false
      }
      seen.add(value)
      return true
    })
    .map((value) => ({ value, label: value }))
}

function normalizeValueForContentType(value: string | undefined, contentType: ContentTypeValue): string {
  const fallback = getDefaultValueForContentType(contentType)
  if (!value) {
    return fallback
  }
  const trimmed = value.trim()
  if (!trimmed) {
    return fallback
  }
  switch (contentType) {
    case 'BOOLEAN':
      return trimmed.toLowerCase() === 'true' ? 'true' : 'false'
    case 'BYTE':
    case 'SHORT':
    case 'INTEGER':
    case 'LONG': {
      const parsed = Number.parseInt(trimmed, 10)
      return Number.isFinite(parsed) ? String(parsed) : fallback
    }
    case 'FLOAT':
    case 'DOUBLE': {
      const parsed = Number.parseFloat(trimmed)
      return Number.isFinite(parsed) ? String(parsed) : fallback
    }
    case 'LIST':
      return isJsonArrayLiteral(trimmed) ? formatJson(trimmed) : fallback
    case 'MAP':
      return isJsonObjectLiteral(trimmed) ? formatJson(trimmed) : fallback
    default:
      return value
  }
}

function getDefaultValueForContentType(contentType: ContentTypeValue): string {
  switch (contentType) {
    case 'BOOLEAN':
      return 'false'
    case 'BYTE':
    case 'SHORT':
    case 'INTEGER':
    case 'LONG':
    case 'FLOAT':
    case 'DOUBLE':
      return '0'
    case 'LIST':
      return '[]'
    case 'MAP':
      return '{}'
    default:
      return ''
  }
}

function validateValueForContentType(value: string, contentType: ContentTypeValue): string | null {
  const trimmed = value.trim()
  if (!trimmed) {
    return '配置内容不能为空'
  }
  switch (contentType) {
    case 'BOOLEAN':
      return isBooleanLiteral(trimmed) ? null : '布尔值仅支持 true 或 false'
    case 'BYTE':
    case 'SHORT':
    case 'INTEGER':
    case 'LONG':
      return isIntegerLiteral(trimmed) ? null : '请输入有效整数'
    case 'FLOAT':
    case 'DOUBLE':
      return isFloatLiteral(trimmed) ? null : '请输入有效数字'
    case 'LIST':
      return isJsonArrayLiteral(trimmed) ? null : '请输入有效 JSON 数组'
    case 'MAP':
      return isJsonObjectLiteral(trimmed) ? null : '请输入有效 JSON 对象'
    default:
      return null
  }
}

function isSameAsExisting(values: FormValues, existing: ConfigItem, labels?: Record<string, string>): boolean {
  const safeContentType: ContentTypeValue = isContentTypeValue(existing.contentType) ? existing.contentType : 'STRING'
  const normalizedExistingValue = normalizeValueForContentType(existing.value, safeContentType)
  const currentLabels = labels ?? existing.labels ?? {}
  const originalLabels = existing.labels ?? {}

  const labelsEqual = JSON.stringify(currentLabels) === JSON.stringify(originalLabels)

  return (
    values.tenant === existing.tenant &&
    values.namespace === existing.namespace &&
    values.appId === existing.appId &&
    values.key === existing.key &&
    values.contentType === safeContentType &&
    values.enabled === existing.enabled &&
    values.value === normalizedExistingValue &&
    labelsEqual
  )
}

function isBooleanLiteral(value: string) {
  const normalized = value.toLowerCase()
  return normalized === 'true' || normalized === 'false'
}

function isIntegerLiteral(value: string) {
  if (!/^[+-]?\d+$/.test(value)) {
    return false
  }
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed)
}

function isFloatLiteral(value: string) {
  if (value.length === 0) {
    return false
  }
  const parsed = Number(value)
  return Number.isFinite(parsed)
}

function isJsonArrayLiteral(value: string) {
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed)
  } catch {
    return false
  }
}

function isJsonObjectLiteral(value: string) {
  try {
    const parsed = JSON.parse(value)
    return parsed !== null && typeof parsed === 'object' && !Array.isArray(parsed)
  } catch {
    return false
  }
}

function formatJson(value: string) {
  try {
    const parsed = JSON.parse(value)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return value
  }
}

function isContentTypeValue(value?: string): value is ContentTypeValue {
  if (!value) {
    return false
  }
  return CONTENT_TYPE_VALUES.includes(value as ContentTypeValue)
}
