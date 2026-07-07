package com.threed.manager.core.system

/**
 * The four rendering tiers the app degrades through as scene size
 * or device capability pressure increases.
 *
 *  - [HIGH]    60 fps, full SH degree 2  — flagship + small scene
 *  - [NORMAL]  30 fps, SH degree 1        — mid-range + medium scene
 *  - [LOW]     15 fps, SH degree 0        — low-end + large scene
 *  - [STATIC]  no live render; emit first-frame image
 *                (Huawei blocklist, no WebGL2, or >500k splats)
 */
enum class RenderTier(val fps: Int, val shDegree: Int) {
    HIGH(60, 2),
    NORMAL(30, 1),
    LOW(15, 0),
    STATIC(0, 0),
}
