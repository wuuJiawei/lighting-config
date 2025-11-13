import { create } from 'zustand'
import type { ConfigItem } from '@/api/types'

interface ConfigEditorState {
  draft?: ConfigItem
  setDraft: (draft?: ConfigItem) => void
  updateContent: (content: string) => void
}

export const useConfigEditorStore = create<ConfigEditorState>((set) => ({
  draft: undefined,
  setDraft: (draft) => set({ draft }),
  updateContent: (content) =>
    set((state) =>
      state.draft
        ? {
            draft: {
              ...state.draft,
              value: content,
            },
          }
        : state,
    ),
}))
