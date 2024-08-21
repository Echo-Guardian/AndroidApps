package com.example.teladecadastro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CameraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val webView = findViewById<WebView>(R.id.webView)


        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true


        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            abrirWebView(webView)
        }
    }

    private fun abrirWebView(webView: WebView) {
        val url = "http://177.220.18.57:5000/video_feed"
        webView.loadUrl(url)
    }

    private fun abrirNavegador() {
        val url = "http://177.220.18.57:5000/video_feed"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}