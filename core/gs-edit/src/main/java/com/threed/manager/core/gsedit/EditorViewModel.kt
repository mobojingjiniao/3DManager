package com.threed.manager.core.gsedit

import com.threed.manager.core.sceneapi.SplatController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pure-domain view model for the editor screen.
 *
 * Responsibilities:
 *  - Owns the in-memory [UndoRedoStack] and the current selection.
 *  - Forwards [applyEdit] calls to the [SplatController] (which will
 *    eventually relay them to the active render backend).
 *  - Surfaces [activeTool] / [selectedNodeIds] for the Compose UI.
 *
 * Not yet wired to:
 *  - Room ([EditLogEntity] persistence)        — Phase 3.x
 *  - Brush / Lasso hit-testing                 — Phase 3.3 follow-up
 *  - Inpaint / cloud round-trip                — Phase 4
 */
class EditorViewModel(
    private val controller: SplatController,
    val undoStack: UndoRedoStack,
    private val scope: CoroutineScope,
) {

    private val _selectedNodeIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNodeIds: StateFlow<Set<Int>> = _selectedNodeIds.asStateFlow()

    private val _activeTool = MutableStateFlow(EditorTool.Select)
    val activeTool: StateFlow<EditorTool> = _activeTool.asStateFlow()

    val undoStackSize: Int get() = undoStack.size
    val canUndo: Boolean get() = undoStack.canUndo
    val canRedo: Boolean get() = undoStack.canRedo

    fun selectNodes(nodes: Set<Int>) {
        _selectedNodeIds.value = nodes
    }

    fun clearSelection() {
        _selectedNodeIds.value = emptySet()
    }

    fun setActiveTool(tool: EditorTool) {
        _activeTool.value = tool
    }

    /**
     * Apply an [EditCommand] to the active scene.
     *
     * Phase 3.3: only pushes to the undo stack. Phase 3.x will also
     * forward to the [SplatController] (which in turn serializes the
     * command to JSON and sends it through the WebBridge).
     */
    fun applyEdit(command: EditCommand) {
        undoStack.push(command)
    }

    /**
     * Pop the most recent command. Phase 3.x will derive the inverse
     * command and forward to the controller; for now it is a pure
     * stack operation so the UI binding can be tested in isolation.
     */
    fun undo(): EditCommand? = undoStack.undo()

    fun redo(): EditCommand? = undoStack.redo()
}
