# 通信机制 & 流程

```mermaid
stateDiagram-v2
    [*] --> Bootstrap
    Bootstrap --> Polling: POST /api/poll (snapshot)
    Polling --> LocalCache: Update arrives
    LocalCache --> ListenerFanout: Notify
    ListenerFanout --> Application
    Application --> LocalCache: Optional refresh request
    Application --> Embedded: local edits (embedded mode)
    Embedded --> Repo: upsert/delete
    Repo --> ServerNotify: ConfigChangedEvent
    ServerNotify --> ChangeFeed: append events
    ChangeFeed --> Polling: next poll读取增量
```
