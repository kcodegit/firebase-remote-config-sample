package com.example.koheiando.firebaseremoteconfigsample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val RC_WELCOME_MSG_KEY = "ab_test_welcome_msg"
        private const val FB_BTN_EVENT_KEY = "welcome_btn"
        private const val FB_BTN_MSG_KEY = "msg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // firebase analytics
        val fbAnalytics = FirebaseAnalytics.getInstance(this)

        // initializing
        val remoteConfig = FirebaseRemoteConfig.getInstance().apply {
            setConfigSettings(
                FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            )
            setDefaults(R.xml.remote_config_defaults)
        }

        // set the default first
        welcome_btn.apply btn@{
            text = remoteConfig.getString(RC_WELCOME_MSG_KEY)
            setOnClickListener {
                // logging to fb
                fbAnalytics.logEvent(FB_BTN_EVENT_KEY, Bundle().apply { putString(FB_BTN_MSG_KEY, this@btn.text.toString()) })
            }
        }

        // for debugging, no cache
        val cacheExpiration =
            if (remoteConfig.info.configSettings.isDeveloperModeEnabled) TimeUnit.HOURS.toSeconds(1) else 0L

        // fetch from FB
        remoteConfig.fetch(cacheExpiration).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "Firebase RemoteConfig fetch() is successful. ")
                remoteConfig.activateFetched()
                welcome_btn.text = remoteConfig.getString(RC_WELCOME_MSG_KEY)
            } else {
                Log.d(TAG, "Firebase RemoteConfig fetch() failed.")
            }
        }
    }
}
