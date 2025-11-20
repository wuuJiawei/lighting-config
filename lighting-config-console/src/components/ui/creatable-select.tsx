import { useEffect, useLayoutEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import * as Popover from '@radix-ui/react-popover'
import { ChevronDown, Plus, Search } from 'lucide-react'
import { cn } from '@/lib/cn'

export interface CreatableSelectOption {
  value: string
  label?: string
}

interface CreatableSelectProps {
  value?: string
  options: CreatableSelectOption[]
  onChange: (value: string) => void
  placeholder?: string
  inputPlaceholder?: string
  emptyText?: string
  disabled?: boolean
}

export function CreatableSelect({
  value,
  options,
  onChange,
  placeholder = '选择或创建',
  inputPlaceholder = '输入后按回车创建 / 选中',
  emptyText = '没有匹配项',
  disabled = false,
}: CreatableSelectProps) {
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [createdOptions, setCreatedOptions] = useState<CreatableSelectOption[]>([])
  const triggerRef = useRef<HTMLButtonElement>(null)
  const [triggerWidth, setTriggerWidth] = useState<number>()

  useLayoutEffect(() => {
    if (triggerRef.current) {
      setTriggerWidth(triggerRef.current.getBoundingClientRect().width)
    }
  }, [open])

  useEffect(() => {
    if (!open) {
      setSearch('')
    }
  }, [open])

  useEffect(() => {
    if (!value) {
      return
    }
    if (optionExists(value, options)) {
      return
    }
    setCreatedOptions((previous) => {
      if (previous.some((item) => item.value === value)) {
        return previous
      }
      return [{ value, label: value }, ...previous]
    })
  }, [value, options])

  const normalizedOptions = useMemo(() => {
    const merged = [...options, ...createdOptions]
    const deduped = merged.filter(
      (option, index, array) => array.findIndex((item) => item.value === option.value) === index,
    )
    if (value && !deduped.some((item) => item.value === value)) {
      deduped.unshift({ value, label: value })
    }
    return deduped
  }, [options, createdOptions, value])

  const trimmedSearch = search.trim()
  const filteredOptions = useMemo(() => {
    if (!trimmedSearch) {
      return normalizedOptions
    }
    const keyword = trimmedSearch.toLowerCase()
    return normalizedOptions.filter((option) => (option.label ?? option.value).toLowerCase().includes(keyword))
  }, [normalizedOptions, trimmedSearch])

  const selected = normalizedOptions.find((item) => item.value === value)
  const canCreate = Boolean(trimmedSearch) && !normalizedOptions.some((item) => item.value === trimmedSearch)
  const firstOption = filteredOptions[0]

  const closePopover = () => setOpen(false)

  const handleSelect = (option: CreatableSelectOption) => {
    onChange(option.value)
    setSearch('')
    closePopover()
    if (!optionExists(option.value, options)) {
      setCreatedOptions((previous) => {
        if (previous.some((item) => item.value === option.value)) {
          return previous
        }
        return [...previous, { value: option.value, label: option.label ?? option.value }]
      })
    }
  }

  const handleCreate = (nextValue: string) => {
    const normalized = nextValue.trim()
    if (!normalized) {
      return
    }
    const created = { value: normalized, label: normalized }
    setCreatedOptions((previous) => {
      if (previous.some((item) => item.value === created.value)) {
        return previous
      }
      return [...previous, created]
    })
    onChange(created.value)
    setSearch('')
    closePopover()
  }

  const handleInputKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault()
      if (firstOption) {
        handleSelect(firstOption)
      } else if (canCreate) {
        handleCreate(trimmedSearch)
      }
    }
    if (event.key === 'Escape') {
      closePopover()
    }
  }

  return (
    <Popover.Root open={open} onOpenChange={(nextOpen) => !disabled && setOpen(nextOpen)}>
      <Popover.Trigger asChild>
        <button
          type="button"
          ref={triggerRef}
          className={cn(
            'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
            !selected?.value && 'text-muted-foreground',
          )}
          disabled={disabled}
        >
          <span className="truncate">{selected?.label ?? selected?.value ?? placeholder}</span>
          <ChevronDown className="h-4 w-4 opacity-50" />
        </button>
      </Popover.Trigger>
      <Popover.Portal>
        <Popover.Content
          sideOffset={6}
          align="start"
          className="z-50 rounded-md border bg-popover p-1 text-popover-foreground shadow-md outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95"
          style={triggerWidth ? { width: triggerWidth } : undefined}
        >
          <div className="flex items-center gap-2 border-b px-3 py-2">
            <Search className="h-4 w-4 text-muted-foreground" />
            <input
              className="flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
              placeholder={inputPlaceholder}
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              onKeyDown={handleInputKeyDown}
              autoFocus
            />
          </div>
          <div className="max-h-64 overflow-y-auto py-1">
            {filteredOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                className={cn(
                  'flex w-full items-center justify-between rounded-sm px-3 py-2 text-left text-sm text-foreground hover:bg-muted',
                  option.value === value && 'bg-muted font-medium',
                )}
                onClick={() => handleSelect(option)}
              >
                <span className="truncate">{option.label ?? option.value}</span>
                {option.value === value ? <span className="text-xs text-muted-foreground">已选</span> : null}
              </button>
            ))}
            {canCreate ? (
              <button
                type="button"
                className="flex w-full items-center gap-2 rounded-sm border border-muted-foreground/40 px-3 py-2 text-left text-sm text-foreground hover:bg-emerald-50"
                onClick={() => handleCreate(trimmedSearch)}
              >
                <Plus className="h-4 w-4" />
                创建并选中 "{trimmedSearch}"
              </button>
            ) : null}
            {!filteredOptions.length && !canCreate ? (
              <p className="px-3 py-2 text-sm text-muted-foreground">{emptyText}</p>
            ) : null}
          </div>
        </Popover.Content>
      </Popover.Portal>
    </Popover.Root>
  )
}

function optionExists(value: string, options: CreatableSelectOption[]) {
  return options.some((item) => item.value === value)
}
