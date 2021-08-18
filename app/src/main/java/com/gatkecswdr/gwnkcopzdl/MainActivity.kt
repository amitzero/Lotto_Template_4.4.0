package com.gatkecswdr.gwnkcopzdl

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import io.flutter.embedding.android.FlutterActivity


class MainActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.default_values)

        if (remoteConfig[SHOW_SPLASH].asBoolean()) splashScreen()

        fetch()
    }

    private fun onFetched() {

//        if (remoteConfig[SHOW_SPLASH].asBoolean()) splashScreen()

        when (remoteConfig[APP_MODE].asLong()) {
            0L -> {
                loadUrl(remoteConfig[URL_0].asString())
            }
            1L -> {
                loadUrl(remoteConfig[URL_1].asString())
            }
            3L -> {
                lottoUi()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl(url: String) {
        val isWeb = remoteConfig[IS_WEBVIEW].asBoolean()
        if (isWeb) {
            web = WebView(this)
            web.settings.javaScriptEnabled = true
            web.settings.domStorageEnabled = true
            web.webViewClient = object : WebViewClient(){
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (view != null) {
                        if (url != null) {
                            view.loadUrl(url)
                            return true
                        }
                    } else {
                        if (url != null) {
                            web.loadUrl(url)
                            return true
                        }
                    }
                    return false
                }
            }
            web.loadUrl(url)
            setContentView(web)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        if (web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun splashScreen() {
        setContentView(R.layout.activity_main)
    }

    private fun lottoUi() {
        val flutterIntent = FlutterActivity.createDefaultIntent(this)
        startActivity(flutterIntent)
        finish()
    }

    private fun fetch() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                } else {
                    Log.d(TAG, "Fetch failed")
//                    Toast.makeText(this, "Fetch failed", Toast.LENGTH_SHORT).show()
                }
//                Toast.makeText(this, ""+task.result+":"+remoteConfig.toString(), Toast.LENGTH_LONG).show()
                onFetched()
            }
    }

    companion object {
        private const val TAG = "MainActivity"

        private const val URL_0 = "URL_0"
        private const val URL_1 = "URL_1"
        private const val APP_MODE = "APP_MODE"
        private const val SHOW_SPLASH = "SHOW_SPLASH"
        private const val IS_WEBVIEW = "IS_WEBVIEW"
    }
}