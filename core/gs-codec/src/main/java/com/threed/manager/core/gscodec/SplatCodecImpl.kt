package com.threed.manager.core.gscodec

import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.min

/**
 * Format detector + decoder for 3DGS assets.
 *
 * Supports:
 *   - PLY (binary little-endian; the de-facto output of most
 *     photogrammetry pipelines including FormScan's intermediate).
 *   - KSPLAT (32-byte compact format by antimatter15/Spark).
 *   - SPLAT (32-byte compact, antimatter15's original layout).
 *   - SPZ / SOG / GLB — detected by extension but not yet decoded;
 *     raise an informative error.
 *
 * Detection is by file extension first, then by magic header ('ply\n').
 */
object SplatCodecImpl {

    fun detect(filename: String, head: ByteArray): Format {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "ply"  -> if (head.size >= 4 && head[0] == 'p'.code.toByte()
                && head[1] == 'l'.code.toByte()
                && head[2] == 'y'.code.toByte()
                && head[3] == '\n'.code.toByte()) Format.Ply else Format.Unknown
            "ksplat" -> Format.Ksplat
            "splat"  -> Format.Splat
            "spz"    -> Format.Spz
            "sog"    -> Format.Sog
            "glb", "gltf" -> Format.Glb
            else -> Format.Unknown
        }
    }

    fun decode(format: Format, input: InputStream): SplatData = when (format) {
        Format.Ply    -> decodePly(input)
        Format.Ksplat -> decodeKsplat(input)
        Format.Splat  -> decodeSplat(input)
        Format.Spz    -> throw UnsupportedOperationException("SPZ decoder not yet implemented (Phase 5+)")
        Format.Sog    -> throw UnsupportedOperationException("SOG decoder not yet implemented")
        Format.Glb    -> throw UnsupportedOperationException("GLB/KHR_gaussian_splatting decoder not yet implemented")
        Format.Unknown -> throw IllegalArgumentException("Unknown splat format")
    }

    private const val SH_C0 = 0.28209479177387814f

    // ──────────────────────────────────────────────────────────
    // PLY (binary little-endian)
    // ──────────────────────────────────────────────────────────

    private enum class PlyType(val byteSize: Int) {
        float(4), double(8), uchar(1), int(4), short(2), uint(4);
        companion object {
            fun parse(s: String): PlyType = valueOf(s.lowercase())
        }
    }

    private data class PlyCol(val name: String, val type: PlyType, val byteOffset: Int)

    private fun decodePly(input: InputStream): SplatData {
        BufferedInputStream(input).use { stream ->
            // Parse header in one pass, accumulating properties.
            val reader = stream.bufferedReader()
            var vertexCount = 0
            val props = mutableListOf<PlyCol>()
            var inVertex = false
            var byteOffset = 0
            var endian = ByteOrder.LITTLE_ENDIAN
            var formatSet = false

            while (true) {
                val line = reader.readLine() ?: throw IllegalStateException("EOF in PLY header")
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("comment") || t.startsWith("obj_info")) continue
                if (t == "end_header") break
                val parts = t.split(Regex("\\s+"))
                when (parts[0]) {
                    "format" -> {
                        when (parts[1]) {
                            "ascii" -> throw UnsupportedOperationException("PLY ASCII not supported (Phase 0 ships binary LE only)")
                            "binary_little_endian" -> { endian = ByteOrder.LITTLE_ENDIAN; formatSet = true }
                            "binary_big_endian" -> { endian = ByteOrder.BIG_ENDIAN; formatSet = true }
                            else -> throw IllegalArgumentException("Unknown PLY format: ${parts[1]}")
                        }
                    }
                    "element" -> {
                        inVertex = (parts[1] == "vertex")
                        if (inVertex) vertexCount = parts[2].toInt()
                        byteOffset = 0
                    }
                    "property" -> {
                        if (inVertex) {
                            val type = PlyType.parse(parts[1])
                            props += PlyCol(parts[2], type, byteOffset)
                            byteOffset += type.byteSize
                        }
                    }
                }
            }
            require(formatSet) { "PLY missing 'format' line" }
            if (vertexCount == 0) return SplatData.empty(Format.Ply)

            val colsByName = props.associateBy { it.name }
            fun col(name: String): PlyCol? = colsByName[name]

            // PLY properties are commonly named in either of two
            // conventions: 3DGS gaussian-splatting extension, or
            // antimatter15-style. Accept both.
            val px = requireNotNull(col("x")) { "PLY missing 'x'" }
            val py = requireNotNull(col("y")) { "PLY missing 'y'" }
            val pz = requireNotNull(col("z")) { "PLY missing 'z'" }
            val sx = col("scale_0") ?: col("sx") ?: throw IllegalArgumentException("PLY missing scale column")
            val sy = requireNotNull(col("scale_1") ?: col("sy")) { "PLY missing scale_y column" }
            val sz = requireNotNull(col("scale_2") ?: col("sz")) { "PLY missing scale_z column" }
            val rw = col("rot_0") ?: col("rot_w") ?: col("q_w")
            val rxc = col("rot_1") ?: col("rot_x") ?: col("q_x")
            val ryc = col("rot_2") ?: col("rot_y") ?: col("q_y")
            val rzc = col("rot_3") ?: col("rot_z") ?: col("q_z")
            val r = col("f_dc_0") ?: col("red")
            val g = col("f_dc_1") ?: col("green")
            val b = col("f_dc_2") ?: col("blue")
            val opacity = col("opacity") ?: col("alpha")

            val positions = FloatArray(vertexCount * 3)
            val scales = FloatArray(vertexCount * 3)
            val rotations = FloatArray(vertexCount * 4)
            val colors = FloatArray(vertexCount * 4)
            val rowSize = byteOffset
            val buffer = ByteArray(rowSize * 64)

            var i = 0
            while (i < vertexCount) {
                val batch = min(64, vertexCount - i)
                val want = batch * rowSize
                var read = 0
                while (read < want) {
                    val n = stream.read(buffer, read, want - read)
                    if (n < 0) throw IllegalStateException("Unexpected EOF in PLY body at vertex $i")
                    read += n
                }
                for (bIdx in 0 until batch) {
                    val rowOff = bIdx * rowSize
                    fun f(c: PlyCol): Float = when (c.type) {
                        PlyType.float -> ByteBuffer.wrap(buffer, rowOff + c.byteOffset, 4).order(endian).float
                        PlyType.double -> ByteBuffer.wrap(buffer, rowOff + c.byteOffset, 8).order(endian).double.toFloat()
                        PlyType.uchar -> (buffer[rowOff + c.byteOffset].toInt() and 0xFF).toFloat()
                        PlyType.int   -> ByteBuffer.wrap(buffer, rowOff + c.byteOffset, 4).order(endian).int.toFloat()
                        PlyType.short -> ByteBuffer.wrap(buffer, rowOff + c.byteOffset, 2).order(endian).short.toFloat()
                        PlyType.uint  -> (ByteBuffer.wrap(buffer, rowOff + c.byteOffset, 4)
                            .order(endian).int.toLong() and 0xFFFFFFFFL).toFloat()
                    }
                    val splatIdx = i + bIdx
                    positions[splatIdx * 3 + 0] = f(px)
                    positions[splatIdx * 3 + 1] = f(py)
                    positions[splatIdx * 3 + 2] = f(pz)
                    scales[splatIdx * 3 + 0] = f(sx)
                    scales[splatIdx * 3 + 1] = f(sy)
                    scales[splatIdx * 3 + 2] = f(sz)
                    if (rw != null && rxc != null && ryc != null && rzc != null) {
                        rotations[splatIdx * 4 + 0] = f(rw)
                        rotations[splatIdx * 4 + 1] = f(rxc)
                        rotations[splatIdx * 4 + 2] = f(ryc)
                        rotations[splatIdx * 4 + 3] = f(rzc)
                    } else {
                        rotations[splatIdx * 4 + 0] = 1f
                    }
                    if (r != null && g != null && b != null) {
                        colors[splatIdx * 4 + 0] = SH_C0 * f(r) + 0.5f
                        colors[splatIdx * 4 + 1] = SH_C0 * f(g) + 0.5f
                        colors[splatIdx * 4 + 2] = SH_C0 * f(b) + 0.5f
                        colors[splatIdx * 4 + 3] = if (opacity != null) {
                            1f / (1f + exp(-f(opacity)))
                        } else 1f
                    } else {
                        colors[splatIdx * 4 + 3] = 1f
                    }
                }
                i += batch
            }

            return SplatData(
                format = Format.Ply,
                splatCount = vertexCount,
                positions = positions,
                scales = scales,
                rotations = rotations,
                colors = colors,
                shCoefficients = FloatArray(0),
                shDegree = 0,
                metadata = mapOf("source" to "ply"),
            )
        }
    }

    // ──────────────────────────────────────────────────────────
    // KSPLAT (32 bytes/splat: 12 pos + 12 scale + 4 rot + 4 color)
    // ──────────────────────────────────────────────────────────

    private fun decodeKsplat(input: InputStream): SplatData {
        val bytes = input.readBytes()
        val n = bytes.size / 32
        if (n * 32 != bytes.size) throw IllegalArgumentException("KSPLAT length not a multiple of 32 (got ${bytes.size})")
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val positions = FloatArray(n * 3); val scales = FloatArray(n * 3)
        val rotations = FloatArray(n * 4); val colors = FloatArray(n * 4)
        for (i in 0 until n) {
            positions[i * 3 + 0] = buf.float
            positions[i * 3 + 1] = buf.float
            positions[i * 3 + 2] = buf.float
            scales[i * 3 + 0] = buf.float
            scales[i * 3 + 1] = buf.float
            scales[i * 3 + 2] = buf.float
            rotations[i * 4 + 0] = buf.float
            rotations[i * 4 + 1] = buf.float
            rotations[i * 4 + 2] = buf.float
            rotations[i * 4 + 3] = buf.float
            colors[i * 4 + 0] = buf.float
            colors[i * 4 + 1] = buf.float
            colors[i * 4 + 2] = buf.float
            colors[i * 4 + 3] = buf.float
        }
        return SplatData(Format.Ksplat, n, positions, scales, rotations, colors,
            FloatArray(0), shDegree = 0, metadata = mapOf("source" to "ksplat"))
    }

    // ──────────────────────────────────────────────────────────
    // SPLAT (32 bytes/splat, 12 pos + 12 scale + 4 color + 4 rot)
    // ──────────────────────────────────────────────────────────

    private fun decodeSplat(input: InputStream): SplatData {
        val bytes = input.readBytes()
        val n = bytes.size / 32
        if (n * 32 != bytes.size) throw IllegalArgumentException("SPLAT length not a multiple of 32 (got ${bytes.size})")
        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val positions = FloatArray(n * 3); val scales = FloatArray(n * 3)
        val rotations = FloatArray(n * 4); val colors = FloatArray(n * 4)
        for (i in 0 until n) {
            positions[i * 3 + 0] = buf.float
            positions[i * 3 + 1] = buf.float
            positions[i * 3 + 2] = buf.float
            scales[i * 3 + 0] = buf.float
            scales[i * 3 + 1] = buf.float
            scales[i * 3 + 2] = buf.float
            colors[i * 4 + 0] = buf.float
            colors[i * 4 + 1] = buf.float
            colors[i * 4 + 2] = buf.float
            colors[i * 4 + 3] = buf.float
            rotations[i * 4 + 0] = buf.float
            rotations[i * 4 + 1] = buf.float
            rotations[i * 4 + 2] = buf.float
            rotations[i * 4 + 3] = buf.float
        }
        return SplatData(Format.Splat, n, positions, scales, rotations, colors,
            FloatArray(0), shDegree = 0, metadata = mapOf("source" to "splat"))
    }
}

/** Convenience top-level decode entry. */
fun decodeSplatFile(format: Format, bytes: ByteArray): SplatData =
    SplatCodecImpl.decode(format, bytes.inputStream())

fun detectSplatFormat(filename: String, bytes: ByteArray): Format {
    val head = bytes.copyOf(min(16, bytes.size))
    return SplatCodecImpl.detect(filename, head)
}