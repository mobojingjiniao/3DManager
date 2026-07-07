package com.threed.manager.core.gscodec

/**
 * 3DGS asset formats the app understands. See plan v2 §2.2 for the
 * full comparison.
 */
enum class Format {
    /** Keith Kellogg compressed format — preferred for mobile. */
    Ksplat,
    /** antimatter15's 32-byte compact format. */
    Splat,
    /** Niantic SPZ compressed format. */
    Spz,
    /** Raw Gaussian Splatting PLY (output of most reconstruction tools). */
    Ply,
    /** PlayCanvas SOG compressed format. */
    Sog,
    /** glTF `KHR_gaussian_splatting` extension. */
    Glb,
    /** Unrecognized / unsupported. */
    Unknown,
}