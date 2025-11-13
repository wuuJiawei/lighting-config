import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useLocation, useNavigate } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'

const schema = z.object({
  token: z.string().min(1, '请输入令牌'),
})

type FormValues = z.infer<typeof schema>

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation() as { state?: { from?: Location } }
  const loginAction = useAuthStore((state) => state.login)
  const [submitting, setSubmitting] = useState(false)
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { token: '' },
  })

  const onSubmit = async (values: FormValues) => {
    setSubmitting(true)
    try {
      const response = await login(values)
      loginAction(response.accessToken)
      toast.success('登录成功')
      const redirectTo = location.state?.from?.pathname ?? '/'
      void navigate(redirectTo, { replace: true })
    } catch (error) {
      console.error('[login] failed', error)
      toast.error('登录失败，请检查令牌')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/30 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>登录 Lighting Config Console</CardTitle>
          <p className="text-sm text-muted-foreground">输入后端配置的访问令牌以继续。</p>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={(event) => void form.handleSubmit(onSubmit)(event)}>
            <div className="space-y-2">
              <label className="text-sm font-medium" htmlFor="token">
                控制台令牌
              </label>
              <Input id="token" type="password" placeholder="例如 lighting-console-token" {...form.register('token')} />
              {form.formState.errors.token ? (
                <p className="text-xs text-destructive">{form.formState.errors.token.message}</p>
              ) : null}
            </div>
            <Button type="submit" className="w-full" disabled={submitting}>
              {submitting ? '登录中...' : '登录'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
