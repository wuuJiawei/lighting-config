import { useEffect, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useForm, type SubmitHandler } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { fetchConfigDetail, upsertConfig } from '@/api/config'
import { PageHeader } from '@/components/shared/page-header'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { useConfigEditorStore } from '@/stores/config-editor'
import { toast } from 'sonner'

const formSchema = z.object({
  tenant: z.string().min(1, '租户必填'),
  namespace: z.string().min(1, '命名空间必填'),
  appId: z.string().min(1, 'App ID 必填'),
  key: z.string().min(1, 'Key 必填'),
  value: z.string().min(1, '配置内容不能为空'),
  contentType: z.enum(['TEXT', 'JSON', 'YAML']),
  labels: z.string().optional(),
  enabled: z.boolean(),
})

type FormValues = z.infer<typeof formSchema>

const DEFAULT_VALUES: FormValues = {
  tenant: 'default',
  namespace: 'default',
  appId: 'default',
  key: '',
  value: '',
  contentType: 'TEXT',
  labels: '',
  enabled: true,
}

export function ConfigEditorPage() {
  const params = useParams<{ configId?: string }>()
  const configId = params.configId
  const isNew = !configId || configId === 'new'
  const navigate = useNavigate()
  const { draft, setDraft, updateContent } = useConfigEditorStore()

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: DEFAULT_VALUES,
  })

  const enabledValue = form.watch('enabled')

  const detailQuery = useQuery({
    queryKey: ['config', configId],
    queryFn: () => fetchConfigDetail(configId!),
    enabled: Boolean(configId) && !isNew,
  })

  useEffect(() => {
    if (detailQuery.data) {
      const { tenant, namespace, appId, key, value, contentType, labels, enabled } = detailQuery.data
      setDraft(detailQuery.data)
      const safeContentType: FormValues['contentType'] = ['TEXT', 'JSON', 'YAML'].includes(contentType)
        ? (contentType as FormValues['contentType'])
        : 'TEXT'
      form.reset({
        tenant,
        namespace,
        appId,
        key,
        value: value ?? '',
        contentType: safeContentType,
        labels: Object.keys(labels ?? {})
          .map((label) => label)
          .join(','),
        enabled,
      })
    } else if (isNew) {
      setDraft(undefined)
      form.reset(DEFAULT_VALUES)
    }
  }, [detailQuery.data, form, isNew, setDraft])

  const valueField = form.register('value')

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      upsertConfig({
        tenant: values.tenant,
        namespace: values.namespace,
        appId: values.appId,
        key: values.key,
        value: values.value,
        contentType: values.contentType,
        labels: toLabelMap(values.labels),
        enabled: values.enabled,
      }),
    onSuccess: (result) => {
      toast.success('配置已保存')
      setDraft(result)
      void navigate(`/configs/${result.id}`)
    },
  })

  const onSubmit: SubmitHandler<FormValues> = (values) => mutation.mutate(values)

  return (
    <div className="space-y-6">
      <PageHeader
        title={isNew ? '新建配置' : `编辑配置 · ${draft?.key ?? configId}`}
        description={isNew ? '按照租户 / 命名空间 / App ID 填充配置内容。' : '在保存前确认启用状态与内容格式。'}
        actions={
          <Button variant="secondary" onClick={() => void navigate('/configs')}>
            返回列表
          </Button>
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
                <Input {...form.register('tenant')} placeholder="default" />
              </Field>
              <Field label="命名空间" error={form.formState.errors.namespace?.message}>
                <Input {...form.register('namespace')} placeholder="default" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="App ID" error={form.formState.errors.appId?.message}>
                <Input {...form.register('appId')} placeholder="lighting-console" />
              </Field>
              <Field label="Key" error={form.formState.errors.key?.message}>
                <Input {...form.register('key')} placeholder="config.example" />
              </Field>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              <Field label="格式">
                <select
                  className="h-10 rounded-md border border-input bg-background px-3 text-sm"
                  {...form.register('contentType')}
                >
                  <option value="TEXT">Properties</option>
                  <option value="JSON">JSON</option>
                  <option value="YAML">YAML</option>
                </select>
              </Field>
              <Field label="标签">
                <Input {...form.register('labels')} placeholder="用逗号分隔，例如 push,gray" />
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
              启用配置（关闭后客户端会收到删除事件）
            </label>
            <Button type="submit" disabled={mutation.isPending} className="w-full">
              {mutation.isPending ? '保存中...' : '保存配置'}
            </Button>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>配置内容</CardTitle>
            <CardDescription>支持 JSON、YAML、Properties 等文本格式</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <Textarea
              {...valueField}
              rows={18}
              placeholder="app.feature=true"
              onChange={(event) => {
                void valueField.onChange(event)
                updateContent(event.target.value)
              }}
            />
            {form.formState.errors.value ? (
              <p className="text-sm text-destructive">{form.formState.errors.value.message}</p>
            ) : null}
          </CardContent>
        </Card>
      </form>
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

function toLabelMap(source?: string): Record<string, string> {
  if (!source) {
    return {}
  }
  const map: Record<string, string> = {}
  source
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .forEach((key) => {
      map[key] = 'true'
    })
  return map
}
