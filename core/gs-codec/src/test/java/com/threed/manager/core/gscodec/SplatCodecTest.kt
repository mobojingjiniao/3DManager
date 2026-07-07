package com.threed.manager.core.gscodec

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Phase 1+ — TDD RED tests for [SplatCodec] format detection and
 * dispatch. Real decoder implementations (PLY / SPLAT / KSPLAT / SPZ /
 * SOG) arrive in Phase 1.3 alongside the live render path.
 */
class SplatCodecTest {

    @Test
    fun `detect returns ksplat for ksplat extension`() {
        assertThat(SplatCodec.detect("scenes/living_room.ksplat")).isEqualTo(Format.Ksplat)
    }

    @Test
    fun `detect returns splat for antimatter15 splat extension`() {
        assertThat(SplatCodec.detect("scenes/butterfly.splat")).isEqualTo(Format.Splat)
    }

    @Test
    fun `detect returns spz for niantic spz extension`() {
        assertThat(SplatCodec.detect("scenes/outdoor.spz")).isEqualTo(Format.Spz)
    }

    @Test
    fun `detect returns ply for raw PLY format`() {
        assertThat(SplatCodec.detect("scenes/raw_mesh.ply")).isEqualTo(Format.Ply)
    }

    @Test
    fun `detect returns unknown for unrecognized extension`() {
        assertThat(SplatCodec.detect("scenes/file.dat")).isEqualTo(Format.Unknown)
    }

    @Test
    fun `decoderFor returns a decoder for known formats`() {
        assertThat(SplatCodec.decoderFor(Format.Ksplat)).isNotNull()
        assertThat(SplatCodec.decoderFor(Format.Splat)).isNotNull()
        assertThat(SplatCodec.decoderFor(Format.Spz)).isNotNull()
        assertThat(SplatCodec.decoderFor(Format.Ply)).isNotNull()
    }

    @Test
    fun `decoderFor returns null for unknown format`() {
        assertThat(SplatCodec.decoderFor(Format.Unknown)).isNull()
    }

    @Test
    fun `decoderFor detects format from path and returns matching decoder`() {
        val decoder = SplatCodec.decoderFor("file.ksplat")
        assertThat(decoder).isNotNull()
    }
}