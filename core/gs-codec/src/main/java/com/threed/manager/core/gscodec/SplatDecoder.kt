package com.threed.manager.core.gscodec

/**
 * Decoder contract for a single 3DGS format.
 *
 * Implementations live alongside [SplatCodec] and are stateless; the
 * codec dispatches to the right one based on [Format]. Decoding is
 * expected to be reasonably fast for the format (≤ 200 ms per 500k
 * splats) and to validate the file magic bytes at the start so that
 * a corrupt file yields a clear error rather than random reads.
 */
interface SplatDecoder {
    /** Format this decoder handles. */
    val format: Format

    /** Validate the file signature. */
    fun probe(bytes: ByteArray): Boolean

    /** Decode the bytes into [SplatData]. May be a slow operation. */
    fun decode(bytes: ByteArray): SplatData
}