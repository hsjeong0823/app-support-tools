package com.hsjeong.supporttools.ui.base

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import java.io.Serializable

open class BaseActivity : ComponentActivity() {
    companion object {
        const val EXTRA_ACTIVITY_INFO = "EXTRA_ACTIVITY_INFO"
    }

    inline fun <reified T : Serializable> Intent.getIntentSerializableExtra(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSerializableExtra(key) as? T
        }
    }
}