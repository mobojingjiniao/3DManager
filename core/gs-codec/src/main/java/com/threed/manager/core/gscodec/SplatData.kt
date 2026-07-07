package com.threed.manager.core.gscodec

/**
 * Memory-uniform representation of a decoded 3DGS asset, suitable for
 * either rendering backend (Filament or Spark/three.js). The actual
 * buffer layout is documented in the implementation; this class only
 * carries the *interpreted* view plus metadata.
 *
 * Memory cost estimate:
 *   N splats × (32 bytes position+scale+rotation+color + N_SH × 16 bytes)
 * For 500k splats at SH degree 0 → ~24 MB.
 */
data class SplatData(
    val format: Format,
    val splatCount: Int,
    val positions: FloatArray,        // [x0,y0,z0, x1,y1,z1, ...]
    val scales: FloatArray,          // [sx0,sy0,sz0, ...]
    val rotations: FloatArray,       // [x0,y0,z0,w0, x1,y1,z1,w1, ...] (quaternions)
    val colors: FloatArray,          // [r0,g0,b0,a0, ...]
    val shCoefficients: FloatArray,  // flattened SH bands 1..N_SH
    val shDegree: Int,               // 0, 1, 2, or 3
    val metadata: Map<String, String> = emptyMap(),
)