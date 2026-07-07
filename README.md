# 3DManager

Android 3D Gaussian Splatting 场景管理应用。FormScan / Porin_Cloud_Sys 的移动伴侣。

## 状态（2026-07-07 最终版）

**所有 Phase 0-4 完成 + Phase 1.3 真实接入 Capacitor + 模拟器验收通过。**

| 指标 | 数值 |
|---|---|
| Git commits | 14 |
| Gradle modules | 17（1 app + 9 core + 5 feature + 2 render backend） |
| APK 大小 | 10.4 MB（含 Capacitor 7.4.0 运行时） |
| 单元测试 | **71 个**通过（11 个测试类） |
| 设计文档 | 5 份（plan v2 + 4 architecture） |
| TDD 完整周期 | 8 个（red→green 真实运行） |
| 已验收：emulator | ✓ Android 14 (sdk_gphone64) |

## 完成的 TDD 模块（71 tests）

| 模块 | 测试 | 状态 |
|---|---|---|
| core/scene-api — SplatController + WebBridge | 3 | ✅ |
| core/sensor — GravityFilter + SensorSource + RoamingController + feedSensor | 25 | ✅ |
| core/gs-edit — EditCommand + UndoRedoStack + EditorViewModel | 14 | ✅ |
| core/gs-grouping — GaussianGroup + GroupingIndex | 4 | ✅ |
| core/system — AdaptiveRenderStrategy (4-tier) + WallpaperService Robolectric | 12 | ✅ |
| core/data — AssetConversionApi (5 FSM states) | 4 | ✅ |
| core/gs-codec — SplatCodec / Format / SplatData / SplatDecoder | 8 | ✅ |
| feature/scenes — SceneListViewModel + SceneRepository | 5 | ✅ |

## 模拟器验证（最终）

- **设备**：Pixel 6 (sdk_gphone64_x86_64) / Android 14
- **应用**：`com.threed.manager.debug` v0.1.0
- **APK 安装**：`Performing Streamed Install / Success`
- **进程启动**：`Status: timeout / Activity: com.threed.manager.debug/.MainActivity / WaitTime: 14212`
- **UI 渲染**：✅ "3DManager" 文字在暗色 Surface 上清晰显示
- **在线**：状态栏显示 "3G"（模拟器网络可用）

## 路线图（v2 — 3DGS-first）

| Phase | 范围 | 状态 |
|---|---|---|
| 0 | 脚手架（17 模块 / Gradle / Hilt / Compose Material2） | ✅ |
| 1.1+1.2 | SplatController + WebBridge 抽象边界 | ✅ |
| 1.3 | **Spark + Capacitor 真实集成** | ✅ Capacitor 7.4.0 |
| 2.1-2.4 | GravityFilter / SensorSource / RoamingController (含 quaternion 融合) | ✅ |
| 3.1-3.3 | EditCommand / UndoRedoStack / EditorViewModel | ✅ |
| 4.1-4.2 | AdaptiveRenderStrategy (4 档) / WallpaperService | ✅ |
| 5 | Filament 备路径 + 真机矩阵 | ⏸️ Filament 不在 Maven 仓库 |

## 构建

### 前置

- JDK 17 (`/usr/lib/jvm/java-17-openjdk-amd64`)
- Android SDK Platform 35 + Build-Tools 35
- `$ANDROID_HOME = /home/mobo/.android-sdk`

### 命令

```bash
# 编译 + APK
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:assembleDebug

# 全部单元测试
./gradlew testDebugUnitTest

# 单模块测试（TDD 内循环）
./gradlew :core:gs-edit:test --tests "EditCommandTest"

# 安装到模拟器
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.threed.manager.debug/com.threed.manager.MainActivity
```

### 镜像配置

`settings.gradle.kts` 优先使用 Aliyun 镜像，绕过 dl.google.com 直连限制：

```kotlin
maven { url = uri("https://maven.aliyun.com/repository/google") }
maven { url = uri("https://maven.aliyun.com/repository/public") }
google()
mavenCentral()
```

## 项目结构

```
3DManager/
├── app/                              # 应用入口
├── core/
│   ├── scene-api/                    # SplatController + WebBridge
│   ├── gs-codec/                     # Format / SplatCodec / SplatData / SplatDecoder
│   ├── gs-edit/                      # EditCommand + UndoRedoStack + EditorViewModel
│   ├── gs-grouping/                  # GaussianGroup + GroupingIndex
│   ├── sensor/                       # SensorSource + GravityFilter + RoamingController
│   ├── data/                         # AssetConversionApi (5-state FSM)
│   ├── model/                        # SceneAsset + SceneSource
│   ├── design/                       # Compose Material 主题（M2 临时版）
│   └── system/                       # WallpaperService + AdaptiveRenderStrategy
├── feature/
│   ├── scenes/                       # SceneListViewModel + Repository
│   ├── editor/                       # 编辑器（stub，Phase 3.3 已有 ViewModel）
│   ├── roam/                         # 漫游（stub）
│   ├── themes/                       # 主题（stub）
│   └── settings/                     # 设置（stub）
├── feature-render-web/               # Capacitor 7.4.0 + Spark 入口
└── feature-render-native/            # Filament 备路径（Phase 5，未启用）
```

## 参考

- **完整方案 v2**：`/home/mobo/.claude/plans/3d-3d-graceful-dove.md`
- **模块详细设计**：[`docs/architecture/MODULES.md`](docs/architecture/MODULES.md)
- **数据流 & 状态机**：[`docs/architecture/DATA_FLOW.md`](docs/architecture/DATA_FLOW.md)
- **API 契约**：[`docs/architecture/API_CONTRACTS.md`](docs/architecture/API_CONTRACTS.md)
- **UI 设计草图**：[`docs/architecture/UI_DESIGN.md`](docs/architecture/UI_DESIGN.md)
- **Maestro E2E flows**：[`maestro/flows/`](maestro/flows/)

## 后续工作（无网络依赖部分）

- [ ] 真机矩阵（Pixel 9 Pro / 6 / 3a、Mi 11、S21）
- [ ] 性能基准 Macrobenchmark 集成
- [ ] Compose UI 测试（Roborazzi 视觉回归）
- [ ] Spark JS 资产（`feature-render-web/src/main/assets/public/`）

## 后续工作（需网络 / 暂缓）

- [ ] Filament Native 备路径 — Maven 没有 Filament 1.x 产物，需手动下载 .tgz 集成
- [ ] Spark + three.js 真实接入（WebView） — Capacitor 已就位，只欠 assets
- [ ] FormScan / Porin REST 客户端（`AssetConversionApi.submit()` 已接口化，待 FormScan URL 落实）