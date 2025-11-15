import { useMemo } from 'react'
import CodeMirror from '@uiw/react-codemirror'
import { json } from '@codemirror/lang-json'
import type { ContentTypeValue } from './content-types'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { cn } from '@/lib/cn'

const INTEGER_TYPES: ContentTypeValue[] = ['BYTE', 'SHORT', 'INTEGER', 'LONG']
const FLOAT_TYPES: ContentTypeValue[] = ['FLOAT', 'DOUBLE']

interface ValueEditorProps {
  contentType: ContentTypeValue
  value: string
  onChange: (value: string) => void
}

export function ValueEditor({ contentType, value, onChange }: ValueEditorProps) {
  if (contentType === 'BOOLEAN') {
    return <BooleanValueEditor value={value} onChange={onChange} />
  }

  if (INTEGER_TYPES.includes(contentType)) {
    return <NumberValueEditor value={value} onChange={onChange} step={1} />
  }

  if (FLOAT_TYPES.includes(contentType)) {
    return <NumberValueEditor value={value} onChange={onChange} step="any" />
  }

  if (contentType === 'LIST' || contentType === 'MAP') {
    return <JsonValueEditor value={value} onChange={onChange} mode={contentType} />
  }

  return <StringValueEditor value={value} onChange={onChange} />
}

function BooleanValueEditor({ value, onChange }: Pick<ValueEditorProps, 'value' | 'onChange'>) {
  const checked = value?.trim().toLowerCase() === 'true'
  return (
    <div className="flex items-center gap-3 rounded-md border border-dashed border-border px-4 py-3">
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        className={cn(
          'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border border-transparent transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
          checked ? 'bg-primary' : 'bg-muted',
        )}
        onClick={() => onChange(checked ? 'false' : 'true')}
      >
        <span
          className={cn(
            'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-background shadow transition',
            checked ? 'translate-x-5' : 'translate-x-1',
          )}
        />
      </button>
      <div className="text-sm">
        <div className="font-medium">{checked ? '已开启 (true)' : '已关闭 (false)'}</div>
        <div className="text-muted-foreground">布尔值仅支持 true / false</div>
      </div>
    </div>
  )
}

interface NumberValueEditorProps extends Pick<ValueEditorProps, 'value' | 'onChange'> {
  step: number | 'any'
}

function NumberValueEditor({ value, onChange, step }: NumberValueEditorProps) {
  return (
    <Input
      type="number"
      inputMode="numeric"
      step={step}
      placeholder={step === 'any' ? '请输入浮点数，例如 3.14' : '请输入整数，例如 42'}
      value={value ?? ''}
      onChange={(event) => onChange(event.target.value)}
    />
  )
}

function JsonValueEditor({ value, onChange, mode }: { value: string; onChange: (value: string) => void; mode: 'LIST' | 'MAP' }) {
  const extensions = useMemo(() => [json()], [])
  const height = mode === 'LIST' ? '280px' : '320px'

  return (
    <div className="border border-border">
      <CodeMirror
        value={value}
        extensions={extensions}
        height={height}
        onChange={(nextValue) => onChange(nextValue)}
        basicSetup={{
          autocompletion: true,
          bracketMatching: true,
          highlightActiveLine: false,
        }}
      />
    </div>
  )
}

function StringValueEditor({ value, onChange }: Pick<ValueEditorProps, 'value' | 'onChange'>) {
  return (
    <Textarea
      rows={10}
      placeholder="输入字符串内容或简单文本，多行自动扩展"
      value={value}
      onChange={(event) => onChange(event.target.value)}
    />
  )
}
