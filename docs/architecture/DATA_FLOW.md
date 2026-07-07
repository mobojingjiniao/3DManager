# 3DManager — 数据流 & 状态机 (Data Flow & State Machines)

> 配套 `plan v2` 的运行期行为规格。包含：状态机、序列图、Room schema、消息流。

---

## 1. SplatSceneState 状态机

```
                  ┌────────┐
       reset()    │  Idle  │  ← 初始态 (SplatController 构造完成)
                  └───┬────┘
                      │ loadScene(path)
                      ▼
                  ┌────────┐
       scene path │Loading │  → UI 显示 skeleton / progress
       stored     └───┬────┘
                      │ WebBridge.loadScene 回调
          ┌───────────┴───────────┐
          ▼                       ▼
    ┌──────────┐           ┌────────┐
    │ Loaded   │           │ Error  │ → UI 显示 error affordance + retry
    │(path,    │           │(cause) │
    │ count)   │           └───┬────┘
    └────┬─────┘               │ loadScene(other) / retry
         │ loadScene(other)    │
         └────────┬────────────┘
                  ▼
              Loading  ← 重新进入
```

**不变量**：
- 重新 `loadScene` 会先卸载旧场景（feature-render-web 的 Spark 内部 GC）
- Error 状态的 `cause` 永远非空
- Loaded 状态的 `splatCount` 可能为 -1（manifest 缺失估算时）

---

## 2. UndoRedoStack 状态机（Phase 3）

```
       push(C1)         push(C2)        push(C3)         undo()
  ┌───┐ ──────▶ ┌───┐ ──────▶ ┌───┐ ──────▶ ┌───┐ ──────▶ ┌───┐
  │ 0 │          │ 1 │         │ 2 │         │ 3 │         │ 2 │
  └───┘          └───┘         └───┘         └───┘         └───┘
   stack=[C1]   stack=[C1,C2] stack=[C1,C2,C3]             redo
                                                             │
                                                          ┌───▼──┐
                                                          │ 3    │
                                                          └──────┘

  push(C4) ←── NEW command after undo (clears redo stack)
  ┌───┐       ┌───┐
  │ 3 │       │ 4 │  stack=[C1,C2,C3,C4]   redo stack=[]  ← 清空
  └───┘       └───┘
```

**关键不变量**：
- max size = 16（内存控制）
- 第 17 条 push 触发最旧条目的 GC
- `undo` 之后 `push` 清空 `redo` 栈（与操作系统文本编辑同语义）
- 不可逆操作（Inpaint 重训练）单独存为 soft-delete layer（Phase 4）

---

## 3. AdaptiveRenderStrategy 决策树

```
                ┌─────────────────┐
                │   Input         │
                │  splat count    │
                │  device profile │
                │  WebGL2?        │
                └────────┬────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │ WebGL2 available?    │
              │  (probe on first run)│
              └──────┬───────────────┘
                     │
       ┌─────────────┴─────────────┐
       ▼ no (rare)                 ▼ yes
   ┌────────────┐           ┌──────────────┐
   │ Backend =  │           │ Device tier? │
   │  STATIC    │           └──────┬───────┘
   │ (no live   │                  │
   │ rendering) │       ┌──────────┼──────────┐
   └────────────┘       ▼ flagship ▼ mid  ▼ low
                        ┌──────────────────┐
                        │ splat count?     │
                        └──┬──────────┬────┘
                           ▼ ≤150k    ▼ >150k
                       ┌─────────┐  ┌────────┐
                       │HIGH 60  │  │NORMAL30│
                       │fps SH2  │  │fps SH1 │
                       └─────────┘  └────────┘
                           ▼ ≤300k
                       ┌─────────┐
                       │NORMAL30 │  → 30 fps
                       │fps SH1  │
                       └─────────┘
                           ▼ >300k
                       ┌─────────┐
                       │LOW 15   │  → 15 fps
                       │fps SH0  │
                       └─────────┘
                           ▼ >500k
                       ┌─────────┐
                       │STATIC   │  → 静态截图
                       └─────────┘
```

**OEM 覆盖**：Huawei EMUI / MIUI / 某些三星版本后台 GPU 节流 → 强制 `STATIC` 兜底

---

## 4. LoadScene 序列图

```
┌──────┐    ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
│ UI   │    │  SceneVM    │    │SplatController│    │  WebBridge   │  (Spark/Capacitor)
└──┬───┘    └──────┬──────┘    └──────┬───────┘    └──────┬───────┘
   │ tap card     │                  │                     │
   ├─────────────▶│  loadScene(path) │                     │
   │              ├─────────────────▶│  _state=Loading     │
   │              │                  │                     │
   │              │                  │  loadScene(json)    │
   │              │                  ├────────────────────▶│
   │              │                  │                     │ 加载资源
   │              │                  │                     │ 解析 KSplat
   │              │                  │                     │ 初始化 WebGL
   │              │                  │ ◀────── Result.OK ─┤
   │              │                  │  _state=Loaded      │
   │              │ ◀─── state ──────┤                     │
   │ ◀─── render ─┤                  │                     │
   │              │                  │                     │
```

---

## 5. Edit Gaussian 序列图（P0 实时编辑）

```
┌──────┐   ┌────────┐   ┌────────────┐   ┌──────────┐   ┌──────────────┐
│  UI  │   │EditorVM│   │UndoRedo    │   │Splat     │   │  WebBridge   │
│      │   │        │   │Stack       │   │Controller│   │  (Spark)     │
└──┬───┘   └───┬────┘   └─────┬──────┘   └────┬─────┘   └──────┬───────┘
   │ Lasso    │               │                │                 │
   ├─────────▶│ applyEdit()   │                │                 │
   │          ├──────────────▶│ push(SetOpacity)│                 │
   │          │               │                │ applyEdit(json) │
   │          │               │                ├────────────────▶│
   │          │               │                │                 │ GPU update
   │          │               │                │ ◀──── Result ───┤
   │          │ ◀─── state ───┤                │                 │
   │ ◀── render                │                │                 │
   │          │               │                │                 │
   │ UNDO     │               │                │                 │
   ├─────────▶│ undo()        │                │                 │
   │          ├──────────────▶│ pop()          │                 │
   │          │               │ inverse cmd    │                 │
   │          │               │                │ applyEdit(json) │
   │          │               │                ├────────────────▶│
   │          │               │                │ ◀──── Result ───┤
   │          │ ◀─── state ───┤                │                 │
   │ ◀── render                │                │                 │
```

**P0 编辑约束**（实时，60 fps）：
- SetOpacity / SetColor / Transform：直接 WebBridge.applyEdit，无离线处理
- 每次操作都 push 到 UndoRedoStack；无磁盘持久化（in-memory）

**P1 离线编辑约束**（Phase 4）：
- Prune + Inpaint：提交云端 API，结果回来再 merge
- 旧 GS layer 保留 → 撤销 = 恢复旧 layer

---

## 6. Live Wallpaper 启动序列

```
┌─────┐   ┌────────────┐   ┌──────────────────┐   ┌──────────────┐
│Wallp│   │Wallpaper   │   │AdaptiveRender    │   │ WebBridge    │
│aper │   │Service     │   │Strategy          │   │ (Spark)      │
│Set  │   │Engine      │   │                  │   │              │
└──┬──┘   └─────┬──────┘   └────────┬─────────┘   └──────┬───────┘
   │ onCreate  │                    │                      │
   │   Engine ├────────────────────▶│ selectTier()         │
   │          │                    │  (splat count + dev) │
   │          │                    │                      │
   │ onSurfaceCreated               │                      │
   │  setup EGL thread (自管)        │                      │
   │          │                    │                      │
   │ onVisibilityChanged(true)      │                      │
   │          ├─────────────────────┼─────────────────────▶│
   │          │                    │   loadScene + render loop (Choreographer)
   │          │                    │                      │
   │          │ 30s 无交互?         │                      │
   │          │ ↓                  │                      │
   │          │ FPS = NORMAL (30)   │                      │
   │          │                    │                      │
   │ onVisibilityChanged(false)     │                      │
   │          │ pause render        │                      │
   │          │ release textures    │                      │
   │          │ but keep Engine     │                      │
```

**关键设计**：
- 自管 EGL 线程，不依赖 GLSurfaceView（WallpaperService 在 View 体系之外）
- Choreographer 在壁纸进程不可靠 → 自管时钟
- 30s 无交互自动降到 NORMAL 30fps
- 离屏时不释放 GL context，只暂停渲染

---

## 7. Room Database Schema (Phase 1+ 渐进)

```sql
-- 场景元数据
CREATE TABLE scene (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  source_path TEXT NOT NULL,         -- 本地路径或 cloud URL
  format TEXT NOT NULL,              -- ply / splat / ksplat / spz / sog
  splat_count INTEGER NOT NULL DEFAULT -1,
  thumbnail_path TEXT,
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL,
  source TEXT NOT NULL DEFAULT 'local',  -- local / formscan / porin
  group_id TEXT                       -- 关联 GaussianGroup (Phase 3)
);
CREATE INDEX idx_scene_updated ON scene(updated_at);

-- 资产（一个 scene 可能含多个 .ksplat / .ply）
CREATE TABLE asset (
  id TEXT PRIMARY KEY,
  scene_id TEXT NOT NULL,
  local_path TEXT NOT NULL,
  size_bytes INTEGER NOT NULL,
  etag TEXT,                          -- HTTP etag
  download_status TEXT NOT NULL,      -- pending / downloading / ready / failed
  FOREIGN KEY (scene_id) REFERENCES scene(id) ON DELETE CASCADE
);

-- 编辑日志（撤销/重做持久化、协作同步）
CREATE TABLE edit_log (
  id TEXT PRIMARY KEY,
  scene_id TEXT NOT NULL,
  command_json TEXT NOT NULL,         -- 序列化的 EditCommand
  inverse_command_json TEXT NOT NULL, -- 撤销用
  author TEXT NOT NULL,               -- 'local' / userId
  timestamp INTEGER NOT NULL,
  FOREIGN KEY (scene_id) REFERENCES scene(id) ON DELETE CASCADE
);
CREATE INDEX idx_edit_log_scene ON edit_log(scene_id, timestamp);

-- 高斯分组缓存（GaussianGrouping 预计算结果）
CREATE TABLE gaussian_group (
  id TEXT PRIMARY KEY,
  scene_id TEXT NOT NULL,
  label TEXT NOT NULL,
  gaussian_indices_blob BLOB NOT NULL,  -- IntArray 序列化
  precomputed_at INTEGER NOT NULL,
  FOREIGN KEY (scene_id) REFERENCES scene(id) ON DELETE CASCADE
);
```

---

## 8. DataStore Preferences

```kotlin
// PreferencesRepository 暴露
val renderBackend: Flow<RendererBackend>   // WebSpark | NativeFilament
val gravitySensitivity: Flow<Float>         // 0.0-2.0, default 1.0
val gravityInvertX: Flow<Boolean>
val gravityInvertZ: Flow<Boolean>
val themePack: Flow<ThemePackId>            // "default" | "midnight" | "..." 
val debugOverlay: Flow<Boolean>
```

---

## 9. 性能数据流（FPS counter / 内存追踪）

```
Spark / Filament ──── 帧时间戳 ───▶ AdaptiveRenderStrategy
                                  └─▶ PerformanceTracker
                                       ├─▶ MemoryPressure (LeakCanary)
                                       └─▶ Macrobenchmark (release flavor)

PerformanceTracker ──── StateFlow ──▶ RoamHud (FPS overlay)
```
