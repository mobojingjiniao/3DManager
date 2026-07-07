package com.threed.manager.core.gscodec

/**
 * Format detection + decoder dispatch.
 *
 * Phase 1.3 ships format detection; the per-format decoder bodies
 * (PLY, SPLAT, KSPLAT, SPZ, SOG) are ported from upstream open-source
 * libraries (antimatter15/splat, Niantic/spz, mkkellogg/GaussianSplats3D)
 * during the same phase.
 */
object SplatCodec {

    /** Detect format from a path or filename. */
    fun detect(path: String): Format {
        val lower = path.lowercase()
        return when {
            lower.endsWith(".ksplat") -> Format.Ksplat
            lower.endsWith(".splat") -> Format.Splat
            lower.endsWith(".spz") -> Format.Spz
            lower.endsWith(".sog") -> Format.Sog
            lower.endsWith(".ply") -> Format.Ply
            lower.endsWith(".glb") || lower.endsWith(".gltf") -> Format.Glb
            else -> Format.Unknown
        }
    }

    /** Look up the decoder for a known format, or null for [Format.Unknown]. */
    fun decoderFor(format: Format): SplatDecoder? = when (format) {
        Format.Ksplat -> StubDecoder(Format.Ksplat)
        Format.Splat -> StubDecoder(Format.Splat)
        Format.Spz -> StubDecoder(Format.Spz)
        Format.Ply -> StubDecoder(Format.Ply)
        Format.Sog -> StubDecoder(Format.Sog)
        Format.Glb -> StubDecoder(Format.Glb)
        Format.Unknown -> null
    }

    /** Convenience: detect + dispatch in one call. */
    fun decoderFor(path: String): SplatDecoder? = decoderFor(detect(path))
}

/**
 * Placeholder decoder used during Phase 0/1 scaffolding. Real decoders
 * arrive in Phase 1.3.
 */
internal class StubDecoder(override val format: Format) : SplatDecoder {
    override fun probe(bytes: ByteArray): Boolean = true  // optimistic
    override fun decode(bytes: ByteArray): SplatData = SplatData(
        format = format,
        splatCount = 0,
        positions = FloatArray(0),
        scales = FloatArray(0),
        rotations = FloatArray(0),
        colors = FloatArray(0),
        shCoefficients = FloatArray(0),
        shDegree = 0,
        metadata = mapOf("stub" to "true"),
    )
}