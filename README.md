# 3DManager

Android 3D Gaussian Splatting 场景管理应用。FormScan / Porin_Cloud_Sys 的移动端伴侣。

## 状态

**Phase 0 — 脚手架完成 (2026-07-07)**

- ✓ Gradle 8.13 wrapper + JDK 17 + AGP 8.7.3
- ✓ 17 个 Gradle 模块 (1 app + 9 core + 5 feature + 2 render backend)
- ✓ `./gradlew :app:assembleDebug` 通过 — 产出 8.6MB APK (min SDK 26, target SDK 35)
- ⏳ Hilt / KSP 已暂时移除（与离线 Maven 缓存协同有问题，Phase 1+ 解决）
- ⏳ Compose Material3 / Capacitor / Filament 推迟到 Phase 1+ / 4 / 5（依赖项需要网络）
- ✅ TDD 流程在 Phase 1 起强制执行（plan v2 详见 docs/plan）

## 项目结构

```
3DManager/
├── app/                              # 应用入口、路由、依赖装配
├── core/
│   ├── scene-api/                    # SplatController + WebBridge + SplatRendererApi
│   ├── gs-codec/                     # .ply/.splat/.ksplat/.spz 解码器（Phase 1）
│   ├── gs-edit/                      # EditCommand + UndoRedoStack（Phase 3）
│   ├── gs-grouping/                  # 高斯实例分组（Phase 3）
│   ├── sensor/                       # SensorSource + GravityFilter（Phase 2）
│   ├── data/                         # Room + DataStore（Phase 1+）
│   ├── model/                        # 序列化模型（Phase 1+）
│   ├── design/                       # Compose Material 主题（Phase 4 升级 M3）
│   └── system/                       # WallpaperService + AdaptiveRenderStrategy（Phase 4）
├── feature/
│   ├── scenes/                       # 3DGS 资产库列表 / 详情（Phase 1.3）
│   ├── editor/                       # 编辑器（Lasso / Gizmo / Inspector） Phase 3
│   ├── roam/                         # 漫游模式（Orbit / FPS / Trackball） Phase 2
│   ├── themes/                       # 主题 + 壁纸（Phase 4）
│   └── settings/                     # 偏好（Phase 4+）
└── feature-render-{web,native}/      # Spark WebView / Filament 双轨
```

## 构建

### 前置

- JDK 17 (`/usr/lib/jvm/java-17-openjdk-amd64`)
- Android SDK Platform 35 + Build-Tools 35
- `$ANDROID_HOME = /home/mobo/.android-sdk` （在 `local.properties` 中配置）

### 命令

```bash
# 完整构建
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew :app:assembleDebug

# 仅模块识别
./gradlew projects

# TDD 内循环（待实现模块时可用）
./gradlew :core:scene-api:test --tests "SplatControllerTest"
```

### 已验证产物

```
app/build/outputs/apk/debug/app-debug.apk   8.6MB
```

## 开发状态（TDD 工作流）

每个新模块遵循严格的 Red → Green → Refactor：

1. **RED**：先在 `src/test/` 写测试，编译失败或断言失败
2. **GREEN**：在 `src/main/` 写最小实现让测试通过
3. **REFACTOR**：抽取抽象（注入接口、协程 scope、依赖反转）

参考 `gradle/libs.versions.toml` 的版本固定与 `core/scene-api/` 的接口设计。

## 路线图

| Phase | 范围 | 状态 |
|---|---|---|
| 0 | 脚手架 + 首构建 | ✅ |
| 1 | Spark + Capacitor 主渲染 | ⏳ next |
| 2 | 重力感应 + 漫游 | ⏳ |
| 3 | 3DGS P0 编辑 | ⏳ |
| 4 | P1 + 自适应 Live Wallpaper + M3 + Theme packs | ⏳ |
| 5 | Filament 备路径 + 真机矩阵 + 性能 | ⏳ |

## 参考

完整方案见工作空间 plan v2：`/home/mobo/.claude/plans/3d-3d-graceful-dove.md`
（包含 3DGS 优先 / Spark vs Filament 双轨 / 5 项原子编辑 / 4 档自适应 Live Wallpaper / 全开源测试）
