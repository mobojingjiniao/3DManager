package com.threed.manager.core.sceneapi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 1.1 — TDD RED tests for SplatController.
 *
 * RED state at the moment of writing:
 *  - `SplatController`, `SplatSceneState`, `WebBridge` are not yet declared.
 *  - These tests must fail to compile, then we declare minimal stubs (Phase 1.2)
 *    that satisfy the type signatures, then the tests still fail at runtime
 *    because the behavior is not implemented, then we implement the behavior
 *    and the tests turn GREEN.
 *
 * Once GREEN, SplatController is the contract that feature-render-web
 * and feature-render-native will plug into with their real WebBridge /
 * SplatRendererApi implementations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SplatControllerTest {

    @Test
    fun `loadScene emits Loaded state when bridge resolves successfully`() = runTest {
        // Given a controller backed by a bridge that resolves
        val bridge = FakeWebBridge().apply { onLoadScene = { _ -> Result.success(Unit) } }
        val controller = SplatController(bridge = bridge, scope = this)

        // When loadScene is called
        controller.loadScene(TestAssets.demoSplatPath)

        // Then the state flow emits Loaded
        val state = controller.state.first { it is SplatSceneState.Loaded }
        assertThat(state).isInstanceOf(SplatSceneState.Loaded::class.java)
    }

    @Test
    fun `loadScene emits Error state when bridge rejects`() = runTest {
        // Given a bridge that rejects
        val failure = IllegalStateException("scene parse failed")
        val bridge = FakeWebBridge().apply { onLoadScene = { _ -> Result.failure(failure) } }
        val controller = SplatController(bridge = bridge, scope = this)

        // When loadScene is called
        controller.loadScene(TestAssets.demoSplatPath)

        // Then the state flow emits Error with the same cause
        val state = controller.state.first { it is SplatSceneState.Error }
        assertThat(state).isInstanceOf(SplatSceneState.Error::class.java)
        assertThat((state as SplatSceneState.Error).cause).isEqualTo(failure)
    }

    @Test
    fun `state starts as Idle before any loadScene call`() = runTest {
        val controller = SplatController(bridge = FakeWebBridge(), scope = this)
        assertThat(controller.state.value).isInstanceOf(SplatSceneState.Idle::class.java)
    }
}
