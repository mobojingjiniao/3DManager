package com.threed.manager.core.gsedit

import com.google.common.truth.Truth.assertThat
import com.threed.manager.core.sceneapi.SplatController
import com.threed.manager.core.sceneapi.SplatSceneState
import com.threed.manager.core.sceneapi.WebBridge
import com.threed.manager.core.sceneapi.SceneManifestJson
import com.threed.manager.core.sceneapi.CameraJson
import com.threed.manager.core.sceneapi.EditCommandJson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Phase 3.3 — TDD RED tests for [EditorViewModel].
 *
 * The view model is the bridge between the Compose UI and the controller
 * + undo stack. It exposes:
 *  - [selectedNodeIds] : current selection
 *  - [activeTool]      : Select / Gizmo / Brush / Lasso
 *  - [undo]/[redo]     : bridge to [UndoRedoStack]
 *  - [applyEdit]       : push to undo + forward to SplatController
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private fun makeVm(scope: kotlinx.coroutines.CoroutineScope): EditorViewModel {
        val controller = SplatController(bridge = FakeWebBridge(), scope = scope)
        return EditorViewModel(controller = controller, undoStack = UndoRedoStack(maxSize = 16), scope = scope)
    }

    @Test
    fun `initial state has no selection and Select tool active`() = runTest {
        val vm = makeVm(this)
        assertThat(vm.selectedNodeIds.value).isEmpty()
        assertThat(vm.activeTool.value).isEqualTo(EditorTool.Select)
    }

    @Test
    fun `selectNodes and clearSelection update the selection state`() = runTest {
        val vm = makeVm(this)
        vm.selectNodes(setOf(1, 2, 3))
        assertThat(vm.selectedNodeIds.value).containsExactly(1, 2, 3)
        vm.clearSelection()
        assertThat(vm.selectedNodeIds.value).isEmpty()
    }

    @Test
    fun `applyEdit pushes the command onto the undo stack`() = runTest {
        val vm = makeVm(this)
        val cmd = EditCommand.SetOpacity(nodeIds = listOf(1, 2), alpha = 0.5f)
        vm.applyEdit(cmd)
        assertThat(vm.undoStackSize).isEqualTo(1)
        assertThat(vm.canUndo).isTrue()
        assertThat(vm.canRedo).isFalse()
    }

    @Test
    fun `undo decrements undo stack and increments redo`() = runTest {
        val vm = makeVm(this)
        vm.applyEdit(EditCommand.SetOpacity(listOf(1), 0.5f))
        vm.applyEdit(EditCommand.SetColor(listOf(2), 1f, 0f, 0f))
        assertThat(vm.undoStackSize).isEqualTo(2)
        vm.undo()
        assertThat(vm.undoStackSize).isEqualTo(1)
        assertThat(vm.canRedo).isTrue()
    }

    @Test
    fun `setActiveTool updates the active tool state`() = runTest {
        val vm = makeVm(this)
        vm.setActiveTool(EditorTool.Brush)
        assertThat(vm.activeTool.value).isEqualTo(EditorTool.Brush)
        vm.setActiveTool(EditorTool.Lasso)
        assertThat(vm.activeTool.value).isEqualTo(EditorTool.Lasso)
    }
}
