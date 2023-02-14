package vla.dracula

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import vla.dracula.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        parseIntent()
        setOnBackPressedDispatcher()
        openWebView(url.toString(), savedInstanceState)
    }

    private fun parseIntent() {
        if (!intent.hasExtra(URL)) {
            throw RuntimeException("Param url is absent")
        }
        url = intent.getStringExtra(URL)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mainWebView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun openWebView(url: String, savedInstanceState: Bundle?) {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        binding.mainWebView.webViewClient = WebViewClient()

        binding.mainWebView.settings.apply {
            binding.mainWebView.settings.domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            databaseEnabled = true
            setSupportZoom(false)
            allowFileAccess = true
            allowContentAccess = true
        }
        if (savedInstanceState != null) binding.mainWebView.restoreState(savedInstanceState)
        else binding.mainWebView.loadUrl(url)
    }

    private fun setOnBackPressedDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.mainWebView.canGoBack()) binding.mainWebView.goBack()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {
        private const val URL = "url"

        fun newIntentAddItem(context: Context, url: String): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(URL, url)
            return intent
        }
    }
}