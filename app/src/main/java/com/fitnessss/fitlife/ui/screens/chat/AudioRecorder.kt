package com.fitnessss.fitlife.ui.screens.chat

import android.media.MediaRecorder
import java.io.File

class AudioRecorder {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    fun start(outputDir: File): File {
        stop()
        outputFile = File(outputDir, "rec_${System.currentTimeMillis()}.m4a")
        val file = outputFile!!

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        startTimeMs = System.currentTimeMillis()
        return file
    }

    fun stop(): Pair<File?, Long> {
        val duration = if (startTimeMs > 0) System.currentTimeMillis() - startTimeMs else 0L
        try {
            recorder?.stop()
        } catch (_: Exception) {}
        recorder?.release()
        recorder = null
        startTimeMs = 0L
        return Pair(outputFile, duration)
    }
}


