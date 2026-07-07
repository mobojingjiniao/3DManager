package com.threed.manager.core.gsedit

import java.util.ArrayDeque

/**
 * In-memory undo/redo history for [EditCommand]s applied to a single scene.
 *
 * Semantics match the typical editor (text, image, 3D modelling):
 *  - [push] appends to the undo stack and clears the redo stack (a new
 *    branch invalidates the previous redo trail).
 *  - [undo] pops from undo, pushes onto redo, returns the popped command.
 *  - [redo] pops from redo, pushes onto undo, returns the popped command.
 *  - [maxSize] bounds memory: the oldest entry is dropped on overflow.
 *  - [clear] empties both stacks (used when the scene is closed or
 *    the user explicitly resets history).
 *
 * Thread-safety: not synchronized. All calls are expected from the
 * single [EditorViewModel] coroutine.
 */
class UndoRedoStack(private val maxSize: Int) {

    init {
        require(maxSize >= 1) { "maxSize must be ≥ 1 but was $maxSize" }
    }

    private val undo: ArrayDeque<EditCommand> = ArrayDeque(maxSize)
    private val redo: ArrayDeque<EditCommand> = ArrayDeque(maxSize)

    /** Number of commands available to undo (read-only). */
    val size: Int get() = undo.size

    val canUndo: Boolean get() = undo.isNotEmpty()
    val canRedo: Boolean get() = redo.isNotEmpty()

    /**
     * Append a command. If the undo stack is at capacity, the oldest
     * entry is dropped. The redo stack is always cleared.
     */
    fun push(command: EditCommand) {
        while (undo.size >= maxSize) undo.pollFirst()
        undo.addLast(command)
        redo.clear()
    }

    /**
     * Move the most recent command from the undo stack onto the redo
     * stack and return it. Returns `null` if the undo stack is empty.
     */
    fun undo(): EditCommand? {
        val cmd = undo.pollLast() ?: return null
        redo.addLast(cmd)
        return cmd
    }

    /**
     * Move the most recently undone command from the redo stack back
     * onto the undo stack and return it. Returns `null` if the redo
     * stack is empty.
     */
    fun redo(): EditCommand? {
        val cmd = redo.pollLast() ?: return null
        undo.addLast(cmd)
        return cmd
    }

    /** Empty both stacks. */
    fun clear() {
        undo.clear()
        redo.clear()
    }
}
