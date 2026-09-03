package com.hsjeong.supporttools

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hsjeong.supporttools.sample.ui.MainActivity
import com.hsjeong.supporttools.ui.main.SupportToolsActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupportToolsLauncherTest {
    @Test
    fun debugApplication_exposesHostAndSupportToolsLaunchers() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val launcherIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(context.packageName)

        val activityNames = context.packageManager
            .queryIntentActivities(launcherIntent, 0)
            .map { it.activityInfo.name }
            .toSet()

        assertEquals(2, activityNames.size)
        assertTrue(activityNames.contains(MainActivity::class.java.name))
        assertTrue(activityNames.contains(SupportToolsActivity::class.java.name))
    }
}
