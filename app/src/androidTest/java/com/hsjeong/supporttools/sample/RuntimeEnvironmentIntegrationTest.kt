package com.hsjeong.supporttools.sample

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hsjeong.supporttools.sample.network.SampleUri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeEnvironmentIntegrationTest {

    @Test
    fun providerInitializesBaseAndDerivedUrlsWithStoredEnvironment() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("SupportToolsPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("server_type", "STG")
            .putBoolean("url_switching_enable", true)
            .commit()

        assertEquals("stg-api.example.test", Uri.parse(SampleUri.Api.baseUrl).authority)
        assertEquals("stg-api.example.test", Uri.parse(SampleUri.Api.DERIVED_URL).authority)
    }
}
