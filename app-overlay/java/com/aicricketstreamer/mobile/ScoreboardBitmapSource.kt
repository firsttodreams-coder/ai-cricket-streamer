package com.aicricketstreamer.mobile

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.view.Surface
import com.pedro.encoder.input.sources.video.VideoSource

class ScoreboardBitmapSource : VideoSource() {
    private val frameLock = Any()
    @Volatile private var latestFrame: Bitmap? = null
    @Volatile private var running = false
    private var surface: Surface? = null
    private var renderThread: Thread? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    fun setFrame(bitmap: Bitmap) {
        synchronized(frameLock) {
            val previous = latestFrame
            latestFrame = bitmap
            if (previous != null && previous !== bitmap && !previous.isRecycled) previous.recycle()
        }
    }

    override fun create(width: Int, height: Int, fps: Int, rotation: Int): Boolean = width > 0 && height > 0

    override fun start(surfaceTexture: SurfaceTexture) {
        if (running) return
        surfaceTexture.setDefaultBufferSize(width, height)
        surface = Surface(surfaceTexture)
        running = true
        renderThread = Thread {
            val interval = (1000L / fps.coerceAtLeast(1))
            while (running) {
                val started = System.currentTimeMillis()
                try {
                    val target = surface
                    if (target != null && target.isValid) {
                        val canvas = target.lockCanvas(null)
                        if (canvas != null) {
                            synchronized(frameLock) {
                                canvas.drawColor(Color.BLACK)
                                latestFrame?.takeIf { !it.isRecycled }?.let {
                                    val scale = minOf(width.toFloat() / it.width, height.toFloat() / it.height)
                                    val left = (width - it.width * scale) / 2f
                                    val top = (height - it.height * scale) / 2f
                                    val save = canvas.save()
                                    canvas.translate(left, top)
                                    canvas.scale(scale, scale)
                                    canvas.drawBitmap(it, 0f, 0f, paint)
                                    canvas.restoreToCount(save)
                                }
                            }
                            target.unlockCanvasAndPost(canvas)
                        }
                    }
                } catch (_: Exception) { }
                val wait = interval - (System.currentTimeMillis() - started)
                if (wait > 0) try { Thread.sleep(wait) } catch (_: InterruptedException) { }
            }
        }.apply { name = "ScoreboardVideoFrames"; start() }
    }

    override fun stop() {
        running = false
        renderThread?.interrupt()
        try { renderThread?.join(1000) } catch (_: InterruptedException) { }
        renderThread = null
        surface?.release()
        surface = null
    }

    override fun release() {
        stop()
        synchronized(frameLock) {
            latestFrame?.let { if (!it.isRecycled) it.recycle() }
            latestFrame = null
        }
    }

    override fun isRunning(): Boolean = running
}
