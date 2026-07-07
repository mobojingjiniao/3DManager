package com.threed.manager.core.gsedit

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 3.1 — TDD RED tests for [UndoRedoStack].
 *
 * The stack follows classic editor semantics:
 *  - push(C)  : append to undo stack, clear redo stack
 *  - undo()   : move top from undo to redo, return it
 *  - redo()   : move top from redo to undo, return it
 *  - canUndo  : undo stack non-empty
 *  - canRedo  : redo stack non-empty
 *  - maxSize  : oldest entry dropped when capacity exceeded
 *
 * Each [EditCommand] is its own inverse (so undo doesn't need a separate
 * "inverse command" record for the in-memory stack). Disk persistence
 * to Room in Phase 3.x will store both directions explicitly so that
 * partial redo across sessions works correctly.
 */
class UndoRedoStackTest {

    @Test
    fun `push then undo returns the same command`() {
        val stack = UndoRedoStack(maxSize = 16)
        val cmd = EditCommand.SetOpacity(nodeIds = listOf(1, 2), alpha = 0.5f)
        stack.push(cmd)
        assertThat(stack.canUndo).isTrue()
        val popped = stack.undo()
        assertThat(popped).isEqualTo(cmd)
        assertThat(stack.canUndo).isFalse()
    }

    @Test
    fun `undo then redo re-applies the same command`() {
        val stack = UndoRedoStack(maxSize = 16)
        val cmd = EditCommand.SetOpacity(listOf(7), 0.8f)
        stack.push(cmd)
        stack.undo()
        val redone = stack.redo()
        assertThat(redone).isEqualTo(cmd)
        assertThat(stack.canRedo).isFalse()
        assertThat(stack.canUndo).isTrue()
    }

    @Test
    fun `three pushes then three undos restore in LIFO order`() {
        val stack = UndoRedoStack(maxSize = 16)
        val c1 = EditCommand.SetOpacity(listOf(1), 0.1f)
        val c2 = EditCommand.SetColor(listOf(2), 1f, 0f, 0f)
        val c3 = EditCommand.Transform(listOf(3), matrix4x4 = listOf(1f, 0f, 0f, 0f))
        stack.push(c1)
        stack.push(c2)
        stack.push(c3)
        assertThat(stack.undo()).isEqualTo(c3)
        assertThat(stack.undo()).isEqualTo(c2)
        assertThat(stack.undo()).isEqualTo(c1)
    }

    @Test
    fun `push after undo clears the redo stack`() {
        val stack = UndoRedoStack(maxSize = 16)
        val c1 = EditCommand.SetOpacity(listOf(1), 0.1f)
        val c2 = EditCommand.SetColor(listOf(2), 1f, 0f, 0f)
        stack.push(c1)
        stack.push(c2)
        stack.undo()  // c2 → redo
        assertThat(stack.canRedo).isTrue()
        stack.push(EditCommand.Prune(listOf(99)))  // new branch
        assertThat(stack.canRedo).isFalse()
    }

    @Test
    fun `undo on empty stack returns null and is idempotent`() {
        val stack = UndoRedoStack(maxSize = 16)
        assertThat(stack.undo()).isNull()
        assertThat(stack.undo()).isNull()  // second call is also null
        assertThat(stack.canUndo).isFalse()
    }

    @Test
    fun `redo on empty stack returns null`() {
        val stack = UndoRedoStack(maxSize = 16)
        assertThat(stack.redo()).isNull()
        assertThat(stack.canRedo).isFalse()
    }

    @Test
    fun `maxSize evicts oldest entry`() {
        val stack = UndoRedoStack(maxSize = 2)
        val c1 = EditCommand.SetOpacity(listOf(1), 0.1f)
        val c2 = EditCommand.SetOpacity(listOf(2), 0.2f)
        val c3 = EditCommand.SetOpacity(listOf(3), 0.3f)
        stack.push(c1)
        stack.push(c2)
        stack.push(c3)  // c1 should be evicted
        val first = stack.undo()  // c3
        val second = stack.undo()  // c2
        assertThat(stack.undo()).isNull()  // c1 was evicted
        assertThat(first).isEqualTo(c3)
        assertThat(second).isEqualTo(c2)
    }

    @Test
    fun `clear empties both stacks`() {
        val stack = UndoRedoStack(maxSize = 16)
        stack.push(EditCommand.SetOpacity(listOf(1), 0.1f))
        stack.push(EditCommand.SetOpacity(listOf(2), 0.2f))
        stack.undo()  // something in redo
        stack.clear()
        assertThat(stack.canUndo).isFalse()
        assertThat(stack.canRedo).isFalse()
    }

    @Test
    fun `size reports current undo stack depth`() {
        val stack = UndoRedoStack(maxSize = 16)
        assertThat(stack.size).isEqualTo(0)
        stack.push(EditCommand.SetOpacity(listOf(1), 0.1f))
        assertThat(stack.size).isEqualTo(1)
        stack.push(EditCommand.SetOpacity(listOf(2), 0.2f))
        assertThat(stack.size).isEqualTo(2)
        stack.undo()
        assertThat(stack.size).isEqualTo(1)
    }
}
