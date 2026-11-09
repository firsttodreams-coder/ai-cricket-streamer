package com.aicricketstreamer.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.webChromeClient = WebChromeClient()
        web.addJavascriptInterface(AppBridge(), "AndroidBridge")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return if (uri.scheme == "http" || uri.scheme == "https") {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    true
                } else false
            }
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class AppBridge {
        @JavascriptInterface
        fun launchScoreboard(url: String, rtmpUrl: String) {
            runOnUiThread {
                val intent = Intent(this@MainActivity, ScoreboardActivity::class.java)
                intent.putExtra(ScoreboardActivity.EXTRA_SCORE_URL, url)
                intent.putExtra(ScoreboardActivity.EXTRA_RTMP_URL, rtmpUrl)
                startActivity(intent)
            }
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
