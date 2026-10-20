package com.hakira.kirasave.data

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

object AudioExtractor {
    fun extractToM4a(context: Context, sourceUri: Uri, destination: File): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var sourceFd: android.content.res.AssetFileDescriptor? = null
        return try {
            sourceFd = context.contentResolver.openAssetFileDescriptor(sourceUri, "r")
                ?: return false
            extractor.setDataSource(
                sourceFd.fileDescriptor,
                sourceFd.startOffset,
                sourceFd.length
            )

            var audioTrack = -1
            var format: MediaFormat? = null
            for (index in 0 until extractor.trackCount) {
                val candidate = extractor.getTrackFormat(index)
                val mime = candidate.getString(MediaFormat.KEY_MIME).orEmpty()
                if (mime.startsWith("audio/")) {
                    audioTrack = index
                    format = candidate
                    break
                }
            }
            if (audioTrack < 0 || format == null) return false
            extractor.selectTrack(audioTrack)

            muxer = MediaMuxer(destination.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrack = muxer.addTrack(format)
            muxer.start()

            val maxSize = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                512 * 1024
            }
            val buffer = ByteBuffer.allocateDirect(maxOf(maxSize, 256 * 1024))
            val info = MediaCodec.BufferInfo()
            while (true) {
                buffer.clear()
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                info.offset = 0
                info.size = size
                info.presentationTimeUs = extractor.sampleTime
                info.flags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    0
                }
                muxer.writeSampleData(muxerTrack, buffer, info)
                extractor.advance()
            }
            muxer.stop()
            true
        } catch (_: Throwable) {
            runCatching { muxer?.stop() }
            false
        } finally {
            runCatching { muxer?.release() }
            runCatching { extractor.release() }
            runCatching { sourceFd?.close() }
        }
    }
}
