package com.aicricketstreamer.mobile

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.sources.audio.NoAudioSource
import com.pedro.library.rtmp.RtmpStream

class ScoreboardActivity : Activity() {
    companion object {
        const val EXTRA_SCORE_URL = "score_url"
        const val EXTRA_RTMP_URL = "rtmp_url"
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var webView: WebView
    private lateinit var status: TextView
    private lateinit var stream: RtmpStream
    private lateinit var frameSource: ScoreboardBitmapSource
    private var streamPrepared = false
    private var isCapturing = false
    private val frameWidth = 1280
    private val frameHeight = 720
    private val captureFrame = object : Runnable {
        override fun run() {
            if (!isCapturing || !::webView.isInitialized || webView.width <= 0 || webView.height <= 0) return
            try {
                val bitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val scale = minOf(frameWidth.toFloat() / webView.width, frameHeight.toFloat() / webView.height)
                val dx = (frameWidth - webView.width * scale) / 2f
                val dy = (frameHeight - webView.height * scale) / 2f
                canvas.drawColor(android.graphics.Color.BLACK)
                canvas.save()
                canvas.translate(dx, dy)
                canvas.scale(scale, scale)
                webView.draw(canvas)
                canvas.restore()
                frameSource.setFrame(bitmap)
            } catch (_: Exception) {
                status.text = "Scoreboard capture error"
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scoreUrl = intent.getStringExtra(EXTRA_SCORE_URL).orEmpty()
        val rtmpUrl = intent.getStringExtra(EXTRA_RTMP_URL).orEmpty()
        if (!scoreUrl.startsWith("https://") && !scoreUrl.startsWith("http://")) {
            finish()
            return
        }
        if (!rtmpUrl.startsWith("rtmp://") && !rtmpUrl.startsWith("rtmps://")) {
            Toast.makeText(this, "Enter a valid RTMP destination first", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF071B18.toInt())
            setPadding(10, 8, 10, 8)
        }
        status = TextView(this).apply {
            text = "Scoreboard ready. Press Start to begin."
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(6, 8, 6, 8)
        }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    status.text = "Scoreboard loaded. Ready to stream."
                }
            }
        }
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val start = Button(this).apply {
            text = "Start scoreboard stream"
            setOnClickListener { startBroadcast(rtmpUrl) }
        }
        val stop = Button(this).apply {
            text = "Stop"
            setOnClickListener { stopBroadcast("Stream stopped") }
        }
        buttons.addView(start, LinearLayout.LayoutParams(0, -2, 2f))
        buttons.addView(stop, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(status)
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(buttons)
        setContentView(root)

        frameSource = ScoreboardBitmapSource()
        stream = RtmpStream(this, object : ConnectChecker {
            override fun onConnectionStarted(url: String) { runOnUiThread { status.text = "Connecting to RTMP server…" } }
            override fun onConnectionSuccess() { runOnUiThread { status.text = "LIVE — scoreboard is streaming" } }
            override fun onConnectionFailed(reason: String) { runOnUiThread { status.text = "Connection failed: $reason" } }
            override fun onDisconnect() { runOnUiThread { status.text = "Disconnected" } }
            override fun onAuthError() { runOnUiThread { status.text = "RTMP authentication failed. Check stream key." } }
            override fun onAuthSuccess() { runOnUiThread { status.text = "RTMP authentication successful" } }
        }, frameSource, NoAudioSource())

        webView.loadUrl(scoreUrl)
        isCapturing = true
        handler.post(captureFrame)
        start.isEnabled = true
        stop.isEnabled = true
    }

    private fun startBroadcast(rtmpUrl: String) {
        if (streamPrepared) {
            status.text = "Stream is already prepared"
            return
        }
        val prepared = stream.prepareVideo(frameWidth, frameHeight, 900 * 1024, 2)
        if (!prepared) {
            status.text = "This phone could not prepare H.264 video encoding"
            return
        }
        streamPrepared = true
        try {
            stream.startStream(rtmpUrl)
            status.text = "Connecting… scoreboard frames are being encoded"
        } catch (e: Exception) {
            status.text = "Could not start stream: ${e.message}"
            streamPrepared = false
        }
    }

    private fun stopBroadcast(message: String) {
        if (::stream.isInitialized && stream.isStreaming) {
            try { stream.stopStream() } catch (_: Exception) { }
        }
        streamPrepared = false
        status.text = message
    }

    override fun onDestroy() {
        isCapturing = false
        handler.removeCallbacks(captureFrame)
        if (::stream.isInitialized) stopBroadcast("Broadcast closed")
        if (::frameSource.isInitialized) frameSource.release()
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
