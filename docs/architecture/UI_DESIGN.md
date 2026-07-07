# 3DManager — UI 设计 (UI Design Sketches)

> 配套 `plan v2` 的屏幕草图与交互规范。Material M2 临时版（Phase 0/1），Phase 4 升级到 M3。
>
> 所有屏幕：竖屏优先；平板自适应横屏（双栏）；深色优先（3DGS 暗背景渲染效果最佳）。

---

## 屏幕清单

| # | 屏幕 | 模块 | 状态 |
|---|---|---|---|
| 1 | SceneListScreen | feature/scenes | Phase 1+ |
| 2 | SceneDetailScreen | feature/scenes | Phase 1+ |
| 3 | RoamScreen | feature/roam | Phase 2 |
| 4 | EditorScreen | feature/editor | Phase 3 |
| 5 | ThemeGalleryScreen | feature/themes | Phase 4 |
| 6 | SettingsScreen | feature/settings | Phase 4 |

---

## 1. SceneListScreen（资产库）

```
┌──────────────────────────────────┐
│ ☰  3DManager              🔍 ⚙  │  ← TopAppBar
├──────────────────────────────────┤
│ ┌─────────────┐ ┌─────────────┐  │
│ │  [缩略图]    │ │  [缩略图]    │  │
│ │             │ │             │  │
│ │ Living Room │ │ Garden      │  │  ← 卡片：缩略图 + 名称
│ │ 500k splats │ │ 1.2M splats │  │
│ │ 2 days ago  │ │ 1 week ago  │  │
│ └─────────────┘ └─────────────┘  │
│                                  │
│ ┌─────────────┐ ┌─────────────┐  │
│ │  [缩略图]    │ │  [缩略图]    │  │
│ │             │ │             │  │
│ │ Kitchen     │ │ Office      │  │
│ │ 350k        │ │ 800k        │  │
│ │ 2 weeks ago │ │ 1 month ago │  │
│ └─────────────┘ └─────────────┘  │
│                                  │
│              ⊕                    │  ← FAB: 导入新场景
├──────────────────────────────────┤
│  资产  │ 主题  │  壁纸  │ 设置   │  ← 底栏（Phase 4+）
└──────────────────────────────────┘
```

**交互**：
- 点击卡片 → 跳转 SceneDetailScreen
- 长按卡片 → 多选模式（删除/分享）
- FAB → 弹出 Photo Picker 导入 .ply/.splat
- 顶栏搜索 → 按名称过滤
- 顶栏菜单 → 切换布局（网格/列表）
- 顶栏设置 → 跳 SettingsScreen

**数据源**：
- 离线优先：Room Scene 表
- 在线时：FormScan/Porin 同步（后台 Worker）

**Roborazzi baseline**：
- `SceneListScreen_empty.png` — 空状态
- `SceneListScreen_4cards.png` — 4 个场景
- `SceneListScreen_long_press.png` — 多选模式

---

## 2. SceneDetailScreen（场景详情 + 预览）

```
┌──────────────────────────────────┐
│ ←  Living Room          ⋮        │
├──────────────────────────────────┤
│                                  │
│                                  │
│        [3DGS 实时渲染]            │  ← WebView (Spark) 占满
│         60 fps · 500k            │  ← FPS / splat count 角标
│                                  │
│                                  │
│                                  │
├──────────────────────────────────┤
│ 500,000 splats · 12.4 MB         │
│ Captured 2026-06-15 on Meta     │
│ Source: FormScan                │
│                                  │
│ [   打开编辑器   ]                │  ← 主操作
│ [   设为壁纸    ]                │  ← Phase 4
│ [   分享链接    ]                │  ← 复制到 Porin
│                                  │
│ ─── 编辑历史 ────────────────    │
│ • SetOpacity(120 nodes) 2h ago   │  ← 显示最近 5 条 edit_log
│ • Prune(50 nodes)        1d ago  │
└──────────────────────────────────┘
```

**交互**：
- 单指拖 = Orbit；双指 = Zoom/Pan
- 顶部 ⋮ → 删除 / 重命名 / 详情
- 底部按钮 → 跳 Editor 或 设为壁纸（Phase 4）
- 渲染区域点按 → 显示该区域高斯分组（Phase 3 屏选）

**状态来源**：
- 实时数据：`SplatController.state`
- 元数据：Room Scene 表
- 编辑历史：Room EditLog 表

---

## 3. RoamScreen（漫游 + HUD）

```
┌──────────────────────────────────┐
│ ←                              ⚙ │  ← 设置（灵敏度、模式）
├──────────────────────────────────┤
│                                  │
│                                  │
│        [3DGS 实时渲染]            │
│                                  │
│              🧭                  │  ← 重力罗盘（右上角小）
│                                  │
│                                  │
│                              ┌──┐│
│                              │  ││  ← FPS 大字
│                              │60││
│                              └──┘│
│                                  │
│  [Orbit] [FPS] [Track] [Cin]    │  ← 模式切换条
│                                  │
│      灵敏度 ──────●─────         │  ← Slider
│                                  │
│  [重置视角]   [保存]              │
└──────────────────────────────────┘
```

**交互（Phase 2 漫游模式）**：

| 模式 | 触发 | 控制 |
|---|---|---|
| Orbit | 单指拖 | 相机绕 target 旋转 |
| FPS | 双指平移 + 摇杆 | 自由移动；陀螺仪叠加转头 |
| Trackball | 单指 | 类似 Orbit 但 z 轴解耦 |
| Cinematic | 自动 | 沿预定义相机轨道播放 |

**重力感应**：
- `TYPE_GAME_ROTATION_VECTOR` 提供世界旋转 → 4 元素 quaternion
- 映射到相机的 lookAt
- 死区（默认 5°）抑制手持抖动
- 灵敏度 0.0-2.0 可调

---

## 4. EditorScreen（编辑 — Phase 3）

```
┌──────────────────────────────────┐
│ ←  编辑 · Living Room    撤销 重做│  ← TopAppBar
├──────────────────────────────────┤
│                                  │
│                                  │
│        [3DGS 实时渲染 + 选区]     │
│                                  │
│         (lasso 圈中一片树)        │
│                                  │
├──────────────────────────────────┤
│ ┌──┬──┬──┬──┐                   │  ← 模式选择
│ │✓ │↔ │○ │/ │                    │    Select/Gizmo/Brush/Lasso
│ └──┴──┴──┴──┘                   │
├──────────────────────────────────┤
│ Inspector · 120 高斯             │  ← 选中后弹出
│                                  │
│  Alpha    ──────●─── 0.5         │  ← 0.0-1.0 slider
│  Color    [■] #FF6B35            │  ← Color picker
│  SH       [-] + 0 - 1 - 2         │  ← 阶数切换
│                                  │
│  [   应用   ]   [   取消   ]      │
│                                  │
│  ── 历史 ────────────────────    │
│  • SetOpacity(120)  2h ago       │  ← 折叠的 undo 栈
│  • SetColor(45)     3h ago       │
└──────────────────────────────────┘
```

**Gizmo（3D 触屏叠层）**：
- 选中后渲染 3 轴 Gizmo（红/绿/蓝）
- 拖拽 Gizmo 轴 → 平移/旋转/缩放
- Phase 3 仅 translate；rotate/scale 视需要

**Lasso / Brush 选区**：
- Brush：点击/滑动 → 多帧投票选高斯
- Lasso：画闭合曲线 → 曲线内高斯
- 显示命中数量（如 "120 高斯"）

**P1 操作入口**（折叠在二级菜单）：
- Inpainting → 上传 mask → 提交云端
- 压缩 / LOD
- 重打光照

---

## 5. ThemeGalleryScreen（主题 + 壁纸 — Phase 4）

```
┌──────────────────────────────────┐
│ ←  主题 & 壁纸           ⊕       │  ← 导入 pack
├──────────────────────────────────┤
│ ─── 当前 ────────────────────    │
│ ┌──────────┐                    │
│ │  预览图   │ Default Dark      │
│ │          │ [系统已应用]        │  ← 当前 Live Wallpaper
│ └──────────┘                    │
│                                  │
│ ─── 可用主题包 ──────────────    │
│ ┌────┐┌────┐┌────┐┌────┐       │
│ │    ││    ││    ││    │        │  ← 横向滚动
│ │默认││午夜││森系││科技│        │
│ │    ││    ││    ││    │        │
│ └────┘└────┘└────┘└────┘       │
│                                  │
│ ─── 渲染档位 ────────────────    │
│ 当前：HIGH (60 fps) · 500k splats│  ← 自适应决策透明化
│ 内存：~180 MB                    │
│                                  │
│ [   设为壁纸   ]                 │
│ [   预览 30s   ]                 │
└──────────────────────────────────┘
```

**自适应档位指示**：
- 显示当前选中的 RenderTier 和依据（splat count + device profile）
- 不可手动选（系统自动）；但展示逻辑帮助用户理解

**Theme Pack 格式**：
```json
{
  "id": "midnight_v1",
  "name": "Midnight",
  "author": "3DManager",
  "color_scheme": { "primary": "#0F1419", ... },
  "default_scene": "scene_xxx",
  "wallpaper_anim": "rotate_30s"
}
```

---

## 6. SettingsScreen（偏好）

```
┌──────────────────────────────────┐
│ ←  设置                          │
├──────────────────────────────────┤
│ ─── 渲染 ──────────────────────   │
│ 后端:    [ Web Spark ▼ ]          │  ← Web Spark / Native Filament
│ FPS 档:  [ 自动 ▼ ]               │  ← 手动 override HIGH/NORMAL/LOW
│ 调试覆盖层:   ◯ 关闭              │  ← FPS / splat count overlay
│                                  │
│ ─── 感应 ──────────────────────   │
│ 灵敏度:    ───●─────  1.0        │
│ 反转 X:    ◯                      │
│ 反转 Z:    ◯                      │
│ 死区:      5°                     │
│                                  │
│ ─── 数据 ──────────────────────   │
│ 存储:      2.3 GB / 64 GB        │  ← 缓存 / 系统配额
│ 清理缓存:  [ 执行 ]               │
│ 离线导出:  [ 执行 ]               │
│                                  │
│ ─── 隐私 ──────────────────────   │
│ 崩溃报告:  ✓ 已启用               │
│ 诊断数据:  ◯ 已禁用               │
│                                  │
│ ─── 关于 ──────────────────────   │
│ 版本:      0.1.0-debug            │
│ 构建:      abc1234  2026-07-07    │
│ 依赖清单:  [ 查看 ]               │
└──────────────────────────────────┘
```

**后端切换约束**：
- Web Spark → Native Filament 需要重启 Spark GL context（约 1-2s）
- 自动回退：Filament 不可用时 → 强制 Web Spark
- 调试覆盖层仅 debug 构建可见

---

## 手势约定（全屏统一）

| 手势 | 行为 |
|---|---|
| 单指拖 | Orbit (Roam) / Scroll (List) |
| 双指捏 | Zoom |
| 双指平移 | FPS move (Roam) / Pan (Editor) |
| 三指 swipe | Undo / Redo |
| 双击 | Reset view / Zoom in |
| 长按 | Multi-select (List) / Picking (Editor) |

| 物理键 | 行为 |
|---|---|
| 音量上 | Undo |
| 音量下 | Redo |
| 电源 | (系统) |

---

## 设计 token（暂用，Phase 4 与 M3 统一）

```kotlin
// 间距
val SpacingXs = 4.dp
val SpacingS = 8.dp
val SpacingM = 16.dp
val SpacingL = 24.dp
val SpacingXl = 32.dp

// 圆角
val RadiusS = 4.dp
val RadiusM = 8.dp
val RadiusL = 16.dp

// 3DGS 渲染区域背景（暗色优先）
val CanvasBackground = Color(0xFF000000)         // 渲染区
val CanvasBorder = Color(0xFF2A2A2A)            // 边框（如果有）
val HudBackground = Color(0xCC000000)           // HUD 半透明
val HudText = Color(0xFFFFFFFF)

// 编辑选区颜色
val SelectionHighlight = Color(0xFF1A73E8)       // 蓝
val LassoStroke = Color(0xFFFF6B35)              // 橙
```

---

## 屏幕适配

| 设备类型 | 布局策略 |
|---|---|
| Phone portrait | 单栏（默认） |
| Phone landscape | 单栏 + 加大渲染区 |
| Tablet portrait | 单栏（主屏）/ 双栏（Editor：左 Inspector 右 3D 视图） |
| Tablet landscape | 双栏（主屏列表 / 详情并排） |
| Foldable | 跟随大屏 |

**Roborazzi 截图按尺寸分桶**：
- `360x800` (Phone portrait)
- `800x360` (Phone landscape)
- `1024x1366` (Tablet portrait)
- `1366x1024` (Tablet landscape)
