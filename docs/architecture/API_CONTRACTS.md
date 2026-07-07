# 3DManager — API 契约 (Contracts)

> 配套 `plan v2` 的接口规范。包含：WebBridge JSON、FormScan / Porin REST 端点、Maestro E2E flow schema。

---

## 1. WebBridge JSON 消息格式

WebBridge 是 core/scene-api 中定义的接口，由 feature-render-web（Spark / Capacitor）和 feature-render-native（Filament）实现。

所有消息用 **kotlinx.serialization JSON** 序列化，snake_case 字段命名（与 Web 生态对齐）。

### 1.1 SceneManifestJson — loadScene 输入

```json
{
  "id": "scene_abc123",
  "path": "/storage/emulated/0/Download/sample.ksplat",
  "format": "ksplat",
  "estimated_splats": 500000,
  "metadata": {
    "title": "Living Room Scan",
    "capture_date": "2026-06-15T10:30:00Z",
    "device": "Meta Spatial",
    "sh_degree": 2
  }
}
```

| 字段 | 类型 | 必须 | 说明 |
|---|---|---|---|
| `id` | string | ✓ | 唯一 ID；用于 Room SceneEntity.id |
| `path` | string | ✓ | 资产绝对路径；或 https:// URL（如果走云端流） |
| `format` | string | ✓ | `ply` / `splat` / `ksplat` / `spz` / `sog` / `glb` |
| `estimated_splats` | long | ✗ | -1 = 未知；用于 AdaptiveRenderStrategy 决策 |
| `metadata` | object | ✗ | 仅展示用，不影响渲染 |

### 1.2 CameraJson — setCamera 输入

```json
{
  "eye": [1.5, 0.3, 2.0],
  "target": [0.0, 0.0, 0.0],
  "up": [0.0, 1.0, 0.0],
  "fov_deg": 60.0,
  "near": 0.05,
  "far": 100.0
}
```

| 字段 | 类型 | 必须 | 说明 |
|---|---|---|---|
| `eye` | [x,y,z] | ✓ | 相机世界坐标 |
| `target` | [x,y,z] | ✓ | 注视点 |
| `up` | [x,y,z] | ✓ | 上方向（默认 [0,1,0]） |
| `fov_deg` | float | ✗ | 视野角度（默认 60） |
| `near` | float | ✗ | 近平面（默认 0.05） |
| `far` | float | ✗ | 远平面（默认 100） |

### 1.3 EditCommandJson — applyEdit 输入

`EditCommandJson` 是 sealed interface，下面是各变体：

#### 1.3.1 SetOpacity（实时，P0）

```json
{
  "op": "set_opacity",
  "node_ids": [12, 13, 14, 15],
  "alpha": 0.4
}
```

#### 1.3.2 SetColor（实时，P0）

```json
{
  "op": "set_color",
  "node_ids": [12, 13],
  "r": 1.0,
  "g": 0.5,
  "b": 0.0
}
```

#### 1.3.3 Transform（实时，P0）

```json
{
  "op": "transform",
  "node_ids": [12],
  "matrix4x4": [1,0,0,0, 0,1,0,0, 0,0,1,0, 0.5,0,0,1]
}
```

> matrix 4×4 row-major；只允许 uniform scale / translation / rotation，不允许非均匀缩放（避免 Gaussian 椭球退化）

#### 1.3.4 Prune（离线，P1）

```json
{
  "op": "prune",
  "node_ids": [10, 11, 12]
}
```

#### 1.3.5 Relight（半实时，P2）

```json
{
  "op": "relight",
  "env_map_path": "/storage/emulated/0/Download/sunset_4k.hdr",
  "sh_degree": 2
}
```

### 1.4 BridgeResult — 同步调用返回值

`WebBridge.loadScene / setCamera / applyEdit` 同步返回 `kotlin.Result<Unit>`，不抛异常跨边界。

错误通过 `Result.failure(Throwable)` 透传：
- `IllegalStateException` — 引擎未就绪
- `IllegalArgumentException` — JSON 解析失败
- `IOException` — 资产加载失败
- `OutOfMemoryError` — 资产超过内存预算

---

## 2. FormScan / Porin 后端 REST 端点（推荐规格）

> 这些是建议端点；3DManager 通过 `core/data` 的 FormScanApi/PorinApi 客户端调用。Phase 1+ 实现。

### 2.1 FormScan API（重建产物）

```
GET  /api/v1/scenes?limit=20&offset=0&sort=updated_at
  Response: SceneListDto
  Auth: Bearer

GET  /api/v1/scenes/{id}
  Response: SceneDto (含 manifest + asset URLs)

GET  /api/v1/scenes/{id}/asset/{assetId}
  Response: 302 redirect to .ksplat / .ply pre-signed URL

POST /api/v1/scenes/{id}/inpaint
  Request: { mask_image: base64png, prompt?: string }
  Response: 202 Accepted { job_id }
  Poll:    GET /api/v1/jobs/{job_id}
           { status: pending|converting|ready|failed, output_scene_id?: string }

POST /api/v1/scenes/{id}/compress
  Request: { target_splats: 500000, format: "ksplat" }
  Response: 202 Accepted { job_id }
```

### 2.2 Porin API（云端浏览 / 共享）

```
GET  /api/v1/shared/{shortId}
  Response: SharedSceneDto (含 scene manifest + 编辑历史公开部分)

POST /api/v1/shared
  Request: { scene_id, edit_log_id_range, is_public }
  Response: { short_id, share_url }
```

### 2.3 本地存储映射

| 远端 | 本地 Room | 本地 DataStore |
|---|---|---|
| Scene | `scene` | — |
| Asset | `asset` | — |
| EditLog | `edit_log` | — |
| — | `gaussian_group` (Phase 3) | — |
| User prefs | — | `preferences` |

---

## 3. Maestro E2E Flow Schema

Maestro flow 是 YAML，存放在 `maestro/flows/`。每个 flow 一个文件，对应 plan §7.2 列出的关键场景。

### 3.1 flow 文件结构

```yaml
# maestro/flows/load_and_view.yaml
appId: com.threed.manager.debug
name: Load and view sample scene
description: Open app, load sample.ksplat, verify FPS overlay
tags: [smoke, phase1]

# Optional: clear state
launchArgs:
  clearState: true

# Optional: mock sensor input
env:
  SENSOR_MOCK_FILE: $ENV_DIR/sensor_idle.json

# Optional: grant permissions
permissions:
  - android.permission.READ_MEDIA_IMAGES
  - android.permission.READ_MEDIA_VIDEO
  - android.permission.POST_NOTIFICATIONS

# Test steps
steps:
  - launchApp
  - assertVisible: "3DManager"
  - tapOn: "Sample scene"
  - assertVisible: "FPS:"
  - extendedWaitUntil:
      visible: "60"
      timeout: 5000
  - takeScreenshot: scenes/loaded.png
```

### 3.2 计划的 flow 文件清单

| 文件 | Phase | 场景 |
|---|---|---|
| `cold_start.yaml` | 1 | 冷启动 ≤ 1.5s |
| `load_and_view.yaml` | 1.3 | 加载 sample.ksplat，FPS ≥ 58 |
| `gravity_rotate.yaml` | 2 | 模拟 sensor 旋转 → 场景视角跟随 |
| `edit_alpha.yaml` | 3 | Lasso + 改 α 0.5 → 60fps 实时 |
| `undo_redo.yaml` | 3 | 连续 5 编辑 + undo 3 + redo 2 |
| `set_wallpaper.yaml` | 4 | 主题 → 设壁纸 → 桌面验证 |
| `compress_lod.yaml` | 4 | 滑条压缩 50% → 内存峰值 -40% |
| `backend_switch.yaml` | 5 | Web Spark → Native Filament 切换 |

### 3.3 Sensor Mock 文件

`$ENV_DIR/sensor_idle.json`:
```json
[
  { "type": "acceleration", "values": [0.0, 0.0, 9.81], "duration_ms": 1000 },
  { "type": "acceleration", "values": [6.93, 0.0, 6.93], "duration_ms": 1000, "comment": "tilt 45deg" }
]
```

注入方式：`adb emu sensor set acceleration 6.93:0:6.93`

---

## 4. Roborazzi 视觉基线 schema

每个被测 Composable 录制一组 baseline 截图：

```
feature/editor/src/test/snapshots/images/
├── recordRoborazzi/
│   ├── MaterialInspector_default.png           # 默认状态
│   ├── MaterialInspector_alpha_high.png       # α = 1.0
│   ├── MaterialInspector_alpha_low.png        # α = 0.0
│   └── GizmoOverlay_selected.png              # 选中态
├── verifyRoborazzi/   ← CI 比对这目录
└── diff/              ← 失败时生成的 diff 图
```

**Roborazzi 配置文件** `feature/editor/src/test/resources/roborazzi.properties`:
```properties
roborazzi.record.filePathStrategy=relativePathFromTestClass
roborazzi.record.resizeScale=1.0
roborazzi.verify.imageComparator=io.github.takahirom.roborazzi.DefaultImageComparator
roborazzi.verify.defaultThreshold=0.04
```

---

## 5. BuildConfig 常量

`app/build/generated/buildConfig/` 包含：

```kotlin
object BuildConfig {
    const val APPLICATION_ID = "com.threed.manager"
    const val VERSION_NAME = "0.1.0"
    const val DEBUG = true
    
    // 渲染后端选择（settings 可改）
    const val DEFAULT_RENDER_BACKEND = "WebSpark"  // "WebSpark" | "NativeFilament"
    
    // Feature flags
    const val ENABLE_LIVE_WALLPAPER = true
    const val ENABLE_INPAINT_PIPELINE = false  // Phase 4+
    const val ENABLE_FILAMENT_FALLBACK = false  // Phase 5+
    
    // Performance budget
    const val MAX_SCENE_SPLATS = 5_000_000L
    const val TARGET_FPS_HIGH = 60
    const val TARGET_FPS_NORMAL = 30
    const val TARGET_FPS_LOW = 15
    
    // Endpoints
    const val FORMSCAN_BASE_URL = "https://api.formscan.local/v1"
    const val PORIN_BASE_URL = "https://api.porin.local/v1"
}
```

---

## 6. CI 命令合约

`.github/workflows/ci.yml` 必须通过的检查：

```bash
# 静态
./gradlew :app:assembleDebug        # 必须能编译
./gradlew :feature:*:lintDebug      # (Phase 5+)

# 单元 + Robolectric
./gradlew testDebugUnitTest         # 全部 module

# 视觉回归（按场景规模分桶，避免 SH 抖动跨桶污染）
./gradlew :feature:editor:verifyRoborazziDebug
./gradlew :feature:roam:verifyRoborazziDebug

# Bench（nightly）
./gradlew :app:benchmarkRelease
```

**通过条件**：
- `assembleDebug` SUCCESS
- `testDebugUnitTest` 全通过
- 视觉回归无差异（阈值 0.04，GPU 抖动已分桶）
- benchmark FPS 退化不超过基线 10%

**失败时**：
- 视觉差异 → 检查 `diff/*.png` 确认是预期变化还是回归
- 用 `git commit -a -m "visual: update MaterialInspector baseline"` 提交新基线
