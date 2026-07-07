# 3DManager — 模块详细设计 (Module Specifications)

> 配套 `plan v2` 的实施细节。每模块列出：职责、关键类、公开 API、依赖关系、TDD 切入点。
>
> 状态：设计稿 (Phase 0 / 1 已部分落地：core/scene-api/ 中 SplatController/WebBridge/SplatSceneState/SplatRendererApi 已 GREEN 测试通过)

---

## 顶层依赖图

```
                          ┌─────────┐
                          │   app   │  ← 入口、Application、MainActivity
                          └────┬────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        ▼                      ▼                      ▼
  ┌───────────┐         ┌────────────┐         ┌────────────┐
  │ feature/* │         │core/system │         │ core/design│
  │  5 模块    │         │ (壁纸/适配) │         │ (主题)     │
  └─────┬─────┘         └─────┬──────┘         └────┬───────┘
        │                     │                     │
        │  ┌──────────────────┴─────────┐           │
        ▼  ▼                            ▼           │
  ┌─────────────┐  ┌──────────────┐  ┌──────────┐  │
  │ core/       │  │ core/        │  │ core/    │  │
  │ gs-*  (3 个)│  │ sensor       │  │ data     │  │
  └──────┬──────┘  └──────┬───────┘  └────┬─────┘  │
         │                │                │        │
         ▼                ▼                ▼        │
  ┌─────────────┐  ┌──────────────┐  ┌──────────┐  │
  │ core/       │  │ core/model   │  │ core/    │◄─┘
  │ scene-api   │◄─┤ (序列化)     │  │ gs-codec │
  │ (核心接口)  │  └──────────────┘  │ (解码器) │
  └─────────────┘                    └──────────┘
         ▲
         │ 实现
         │
  ┌──────┴────────────┐
  │ feature-render-*  │  (2 个：Web Spark / Native Filament)
  └───────────────────┘
```

---

## app/ — 应用入口

| 项目 | 内容 |
|---|---|
| **职责** | 装配 Hilt 子图（Phase 1+）、路由到首屏、声明 Application 类 |
| **关键类** | `ThreeDManagerApp`（Application）、`MainActivity`（单 Activity） |
| **公开 API** | 无（消费所有 feature 模块） |
| **依赖** | 所有 `core/*` + 所有 `feature/*` + 两个 render backend |
| **TDD 切入点** | 无业务逻辑，仅 Compose 装配；UI 测试由 Preview/Roborazzi 覆盖 |
| **当前状态** | Phase 0.5 完成；MainActivity 渲染 PlaceholderScreen |

---

## core/scene-api/ — 场景控制核心 ✅ 部分完成

| 项目 | 内容 |
|---|---|
| **职责** | 抽象 3DGS 场景控制器；定义 Web/Native 渲染后端边界 |
| **关键类** | `SplatController`、`SplatSceneState`（FSM）、`WebBridge`（interface）、`SplatRendererApi`（interface）、`SceneManifestJson`、`CameraJson`、`EditCommandJson`（sealed） |
| **公开 API** | `SplatController.state: StateFlow<SplatSceneState>`、`SplatController.loadScene(path)`、`SplatController.setCamera(...)`、`SplatController.applyEdit(cmd)` |
| **依赖** | kotlinx-serialization, coroutines |
| **TDD 切入点** | `SplatControllerTest.kt` 3 个测试 (Idle/Loaded/Error) 已 GREEN |
| **当前状态** | Phase 1.2 完成 |
| **后续任务** | Phase 1.3 接入真实 WebBridge（Capacitor）；Phase 3 接入 EditCommand → bridge 转发 |

---

## core/gs-codec/ — 3DGS 资产解码器

| 项目 | 内容 |
|---|---|
| **职责** | 解析 `.ply` / `.splat` / `.ksplat` / `.spz` / `.sog` 资产到统一内存表示 `SharedSplatData` |
| **关键类** | `SplatCodec`（工厂/接口）、`PlyDecoder`、`SplatDecoder`、`KsplatDecoder`、`SpzDecoder`、`SogDecoder`、`SharedSplatData`（gaussians: FloatBuffer、shCoefficients: FloatArray、metadata） |
| **公开 API** | `SplatCodec.detect(path): Format`、`SplatCodec.decode(inputStream): SharedSplatData`、`SplatCodec.estimateMemory(path): Long` |
| **依赖** | core/model（manifest 类型） |
| **TDD 切入点** | 解码公开样本 → 高斯数比对；属性字段解析；SH 系数保留；损坏文件 → Result.failure |
| **当前状态** | 空模块（Phase 1 启动） |
| **实现参考** | antimatter15/splat (.splat 32B 格式)、Niantic/spz (.spz 压缩)、PlayCanvas SuperSplat 工具链 |

---

## core/gs-edit/ — 3DGS 编辑模型（Phase 3）

| 项目 | 内容 |
|---|---|
| **职责** | 5 项原子操作的数据结构与撤销/重做栈；EditLog 持久化 |
| **关键类** | `EditCommand`（sealed：TransformNodes/SetOpacity/SetColor/Prune/Relight）、`UndoRedoStack`（max 16 步）、`EditLogRepository`（Room） |
| **公开 API** | `UndoRedoStack.push(cmd)`、`pop()`、`canUndo`、`canRedo`、`undo()`、`redo()` |
| **依赖** | core/data (Room) |
| **TDD 切入点** | RED 优先：3 个状态（push/undo/redo 顺序）；FIFO/LIFO 行为；maxSize 截断；新 push 清空 redo 栈 |
| **当前状态** | 空模块 |
| **关键不变量** | 撤销栈只保留 commandJson，不存整份 .ksplat（节省内存） |

---

## core/gs-grouping/ — 高斯实例分组（Phase 3）

| 项目 | 内容 |
|---|---|
| **职责** | 预计算 + 运行时选择高斯子集（按 ID、3D Box、2D 屏选） |
| **关键类** | `GaussianGroup`（id + gaussianIndices + label）、`GroupingIndex`（O(1) id → index）、`ScreenSpaceSelector`（picker）、`LassoSelector`（多帧投票） |
| **公开 API** | `ScreenSpaceSelector.pickAt(x, y, camera): NodeId`、`LassoSelector.select(points): Set<NodeId>` |
| **依赖** | core/scene-api（camera state） |
| **TDD 切入点** | 已知相机 + 已知 splat → 屏选返回正确 NodeId；lasso 多点求凸包内高斯 |

---

## core/sensor/ — 传感器抽象（Phase 2）

| 项目 | 内容 |
|---|---|
| **职责** | 抽象重力/旋转矢量；低通滤波避免抖动 |
| **关键类** | `SensorSource`（interface）、`AndroidSensorSource`（SensorManager）、`FakeSensorSource`（测试）、`GravityFilter`（α=0.5 低通滤波） |
| **公开 API** | `SensorSource.gravity: Flow<GravityVector>`、`SensorSource.rotation: Flow<RotationVector>` |
| **依赖** | 无 |
| **TDD 切入点** | Phase 2.1 优先：GravityFilter RED（首样本透传、α=0.5 指数平滑、模长守恒） |
| **当前状态** | 空模块 |
| **关键设计** | 用 `TYPE_GAME_ROTATION_VECTOR` 而非欧拉角 → 避免万向锁 |

---

## core/data/ — 持久化（Phase 1+）

| 项目 | 内容 |
|---|---|
| **职责** | Room 数据库（scenes / assets / edit_log / group_cache）+ DataStore 偏好 |
| **关键类** | `ThreeDManagerDatabase`、`SceneEntity`、`AssetEntity`、`EditLogEntity`、`GroupCacheEntity`、`PreferencesRepository` |
| **公开 API** | `PreferencesRepository.observeBackend(): Flow<RendererBackend>` 等 |
| **依赖** | Room, DataStore, kotlinx-serialization |
| **TDD 切入点** | Robolectric + in-memory Room；DAO 单元测试；DataStore preferences round-trip |
| **当前状态** | 编译通过（无实际 Entity/DAO） |

---

## core/model/ — 序列化模型

| 项目 | 内容 |
|---|---|
| **职责** | 纯 JVM 模块，定义 manifest / config / theme pack 的 `@Serializable` 数据类 |
| **关键类** | `SceneManifest`、`SplatAsset`、`ThemePack`、`EditLogEntry`、`BackendConfig` |
| **公开 API** | 全是 data class；通过 JSON I/O 与外部系统（FormScan/Porin）交互 |
| **依赖** | 仅 kotlinx-serialization（无 Android） |
| **TDD 切入点** | property-based testing (kotest) — round-trip JSON 编解码 |

---

## core/design/ — Material 主题 ✅ 部分完成

| 项目 | 内容 |
|---|---|
| **职责** | Compose 主题（Material M2 临时，Phase 4 升级 M3） |
| **关键类** | `Theme`、`ThemePack` 数据类（Phase 4） |
| **公开 API** | `Theme(useDarkTheme, content)` |
| **依赖** | Compose Material M2（缓存可用） |
| **当前状态** | Phase 0 完成（M2 临时版） |

---

## core/system/ — 系统级服务 ✅ 部分完成

| 项目 | 内容 |
|---|---|
| **职责** | WallpaperService、AdaptiveRenderStrategy、MediaStore 写入 |
| **关键类** | `ThreeDManagerWallpaperService`（占位）、`AdaptiveRenderStrategy`（占位，Phase 4 实现 4 档）、`MediaStoreWriter`（Phase 4） |
| **公开 API** | `AdaptiveRenderStrategy.selectTier(): Tier` |
| **依赖** | 无 |
| **当前状态** | 占位 stub（Phase 4 实现） |

---

## feature/scenes/ — 3DGS 资产库

| 项目 | 内容 |
|---|---|
| **职责** | 列表 + 详情 + 预览屏；加载场景用 SplatController |
| **关键类** | `SceneListViewModel`、`SceneListScreen`、`SceneDetailViewModel`、`SceneDetailScreen` |
| **公开 API** | 仅内部 API |
| **依赖** | core/scene-api, core/data, core/model, core/design |
| **TDD 切入点** | ViewModel RED（用 Turbine 测 StateFlow 发射） |

---

## feature/editor/ — 3DGS 编辑器（Phase 3）

| 项目 | 内容 |
|---|---|
| **职责** | Brush / Lasso / Gizmo / Inspector 屏；将 EditCommand 桥接到 WebBridge |
| **关键类** | `EditorViewModel`、`EditorScreen`、`MaterialInspector`、`LightInspector`、`GizmoOverlay`、`LassoBrush` |
| **公开 API** | 仅内部 |
| **依赖** | core/gs-edit, core/gs-grouping, core/scene-api |
| **TDD 切入点** | EditCommand RED（α/Color/Transform 前后值断言） + Roborazzi Inspector 截图 |

---

## feature/roam/ — 漫游模式（Phase 2）

| 项目 | 内容 |
|---|---|
| **职责** | Orbit / FPS / Trackball 控制器；将 SensorSource 桥接到 WebBridge.setCamera |
| **关键类** | `RoamingController`、`RoamScreen`、`RoamHud`（FPS / 模式 / 灵敏度） |
| **公开 API** | 仅内部 |
| **依赖** | core/scene-api, core/sensor |
| **TDD 切入点** | RoamingController（Orbit 拖拽旋转 / FPS 双指 / 重力 deadband 抑制） |

---

## feature/themes/ — 主题 + 壁纸（Phase 4）

| 项目 | 内容 |
|---|---|
| **职责** | 主题包管理、Live Wallpaper 预览、设为系统壁纸 |
| **关键类** | `ThemeGalleryViewModel`、`ThemeGalleryScreen`、`WallpaperPreviewActivity`、`WallpaperApplyService` |
| **依赖** | core/system, core/data, core/design |
| **TDD 切入点** | AdaptiveRenderStrategy tier 决策 TDD（场景规模 × 设备等级） |

---

## feature/settings/ — 偏好

| 项目 | 内容 |
|---|---|
| **职责** | 重力灵敏度、渲染后端选择（Web Spark / Native Filament）、隐私 |
| **关键类** | `SettingsViewModel`、`SettingsScreen` |
| **依赖** | core/data, core/design |
| **TDD 切入点** | ViewModel 状态机 + DataStore round-trip |

---

## feature-render-web/ — Spark + Capacitor 主渲染 ✅ Stub

| 项目 | 内容 |
|---|---|
| **职责** | Phase 1.3+：Capacitor 容器 + Spark + three.js Web 渲染；通过 `WebBridge` 与 SplatController 通信 |
| **关键类** | `SplatBridgePlugin`（Capacitor plugin）、`index.html`（importmap）、`render.js`（three.js + spark）、`bridge.js`（JS bridge） |
| **公开 API** | `WebBridge`（实现 core/scene-api/ 的接口） |
| **依赖** | core/scene-api；Capacitor 6.x（Phase 1.3 需网络下载） |
| **当前状态** | Stub 编译通过；Capacitor 依赖待网络解锁后添加 |
| **集成方式** | `app` 启动时通过 Hilt + BuildConfig.RENDER_BACKEND 注入 |

---

## feature-render-native/ — Filament Native 备渲染 ✅ Stub

| 项目 | 内容 |
|---|---|
| **职责** | Phase 5：Filament 1.72.1 + glTF KHR_gaussian_splatting；自写 .splat → .glb 转换器 |
| **关键类** | `FilamentSplatRenderer`、`GlbGsEncoder`、`KhrGaussianSplatting` |
| **公开 API** | `SplatRendererApi`（实现 core/scene-api/ 的接口） |
| **依赖** | core/scene-api, core/gs-codec；Filament aar（Phase 5 需网络下载） |
| **当前状态** | Stub 编译通过 |

---

## 实施优先级（与 plan §6 对应）

| 顺序 | 模块 | Phase |
|---|---|---|
| 1 | core/scene-api | 1.1-1.2 ✅ |
| 2 | feature-render-web | 1.3 (网络) |
| 3 | core/sensor | 2.1 (GravityFilter) |
| 4 | feature/roam | 2.2 |
| 5 | core/data | 1+ 渐进 |
| 6 | core/gs-edit | 3.1 |
| 7 | core/gs-grouping | 3.2 |
| 8 | feature/editor | 3.3 |
| 9 | core/gs-codec | 1.3+ |
| 10 | feature/themes | 4.1 |
| 11 | core/system (Wallpaper) | 4.2 |
| 12 | core/design (M3) | 4.3 |
| 13 | feature-render-native | 5.1 |
| 14 | feature/scenes / feature/settings | 持续 |
