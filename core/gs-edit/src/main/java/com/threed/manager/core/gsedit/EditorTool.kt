package com.threed.manager.core.gsedit

/**
 * The four interaction modes exposed by the editor toolbar.
 *
 *  - [Select] : no-op selection; tap selects, drag pans the camera
 *  - [Gizmo]  : 3-axis translate / rotate / scale Gizmo overlays
 *  - [Brush]  : freehand selection (multi-frame vote on hit-test)
 *  - [Lasso]  : closed-curve selection
 */
enum class EditorTool { Select, Gizmo, Brush, Lasso }
