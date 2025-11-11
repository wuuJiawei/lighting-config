# Client 模块流程

```mermaid
flowchart TD
    A[Start LightingClient] --> B[POST /api/poll (lastVersion=0)]
    B --> C[Populate Caffeine cache]
    C --> D[Schedule next poll (poll-interval)]
    D --> E[POST /api/poll (lastVersion=n)]
    E --> F{Response contains changes?}
    F -- Yes --> G[Update cache + version]
    G --> H[Notify ListenerRegistry]
    H --> D
    F -- No --> D
```
