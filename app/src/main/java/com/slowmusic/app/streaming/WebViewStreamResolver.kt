package com.slowmusic.app.streaming

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.slowmusic.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class WebViewStreamResolver @Inject constructor() {
    suspend fun resolve(videoId: String): ResolvedStream? = Companion.resolve(videoId)

    companion object {
        private val main = Handler(Looper.getMainLooper())
        private val webViewRef = AtomicReference<WebView?>()
        private const val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36"

        @SuppressLint("SetJavaScriptEnabled")
        fun init(activity: Activity): () -> Unit {
            val webView = WebView(activity).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.userAgentString = UA
                layoutParams = ViewGroup.LayoutParams(1, 1)
            }
            webViewRef.set(webView)
            return { runCatching { webView.destroy() }; webViewRef.set(null) }
        }

        suspend fun resolve(videoId: String, timeoutMs: Long = 20_000): ResolvedStream? = withContext(Dispatchers.Main) {
            val webView = webViewRef.get() ?: return@withContext null
            suspendCancellableCoroutine { cont ->
                var done = false
                fun finish(url: String?) {
                    if (done) return
                    done = true
                    webView.stopLoading()
                    webView.webViewClient = WebViewClient()
                    cont.resume(url?.let {
                        ResolvedStream(
                            url = it,
                            userAgent = UA,
                            headers = mapOf("Referer" to "https://www.youtube.com/", "Origin" to "https://www.youtube.com"),
                            source = "webview"
                        )
                    })
                }
                val timeout = Runnable { Logger.w("WebViewResolver", "Timeout for $videoId"); finish(null) }
                main.postDelayed(timeout, timeoutMs)
                cont.invokeOnCancellation { main.removeCallbacks(timeout) }
                webView.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): android.webkit.WebResourceResponse? {
                        val u = request.url.toString()
                        if ((u.contains("googlevideo.com") || u.contains("videoplayback")) && (u.contains("mime=audio") || u.contains("itag="))) {
                            main.post { main.removeCallbacks(timeout); finish(u) }
                        }
                        return null
                    }
                }
                webView.loadUrl("https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1")
            }
        }
    }
}
