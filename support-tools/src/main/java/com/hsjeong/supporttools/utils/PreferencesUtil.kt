package com.hsjeong.supporttools.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.edit
import com.hsjeong.supporttools.constants.Constants.Preference
import java.io.File

data class PreferenceItem(
    val key: String,
    val value: Any?,
    val type: PrefType
)

enum class PrefType { STRING, INT, BOOLEAN, FLOAT, LONG, UNKNOWN }

class PreferencesUtil {
    companion object {
        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(Preference.PREFS_NAME, Context.MODE_PRIVATE)
        }

        internal fun putBooleanPreferences(context: Context, key: String, value: Boolean) {
            getSharedPreferences(context).edit(true) { putBoolean(key, value) }
        }

        internal fun putStringPreferences(context: Context, key: String, value: String) {
            getSharedPreferences(context).edit(true) { putString(key, value) }
        }

        internal fun getBooleanPreferences(context: Context, key: String, defaultValue: Boolean = false): Boolean {
            return getSharedPreferences(context).getBoolean(key, defaultValue)
        }

        internal fun getStringPreferences(context: Context, key: String, defaultValue: String? = null): String? {
            return getSharedPreferences(context).getString(key, defaultValue)
        }

        internal fun setScreenNameOverLayEnable(context: Context, boolean: Boolean) {
            putBooleanPreferences(context, Preference.KEY_SCREEN_NAME_OVERLAY_ENABLE, boolean)
        }

        internal fun getScreenNameOverLayEnable(context: Context): Boolean {
            return getBooleanPreferences(context, Preference.KEY_SCREEN_NAME_OVERLAY_ENABLE, true)
        }

        internal fun setLogcatViewerEnable(context: Context, boolean: Boolean) {
            putBooleanPreferences(context, Preference.KEY_LOGCAT_VIEWER_ENABLE, boolean)
        }

        internal fun getLogcatViewerEnable(context: Context): Boolean {
            return getBooleanPreferences(context, Preference.KEY_LOGCAT_VIEWER_ENABLE, true)
        }

        internal fun setLogcatViewerSearchWord(context: Context, value: String) {
            putStringPreferences(context, Preference.KEY_LOGCAT_VIEWER_SEARCH_WORD, value)
        }

        internal fun getLogcatViewerSearchWord(context: Context): String? {
            return getStringPreferences(context, Preference.KEY_LOGCAT_VIEWER_SEARCH_WORD, "")
        }

        internal fun setNetworkLogEnable(context: Context, boolean: Boolean) {
            putBooleanPreferences(context, Preference.KEY_NETWORK_LOG_ENABLE, boolean)
        }

        internal fun getNetworkLogEnable(context: Context): Boolean {
            return getBooleanPreferences(context, Preference.KEY_NETWORK_LOG_ENABLE, true)
        }

        internal fun setUrlSwitchingEnable(context: Context, boolean: Boolean) {
            putBooleanPreferences(context, Preference.KEY_URL_SWITCHING_ENABLE, boolean)
        }

        internal fun getUrlSwitchingEnable(context: Context): Boolean {
            return getBooleanPreferences(context, Preference.KEY_URL_SWITCHING_ENABLE, true)
        }

        internal fun setDeeplinkSearchUrl(context: Context, value: String) {
            putStringPreferences(context, Preference.KEY_DEEPLINK_SEARCH_URL, value)
        }

        internal fun getDeeplinkSearchUrl(context: Context): String? {
            return getStringPreferences(context, Preference.KEY_DEEPLINK_SEARCH_URL, "")
        }

        // SharedPreferences 파일 찾기
        internal fun getPreferenceFiles(context: Context): List<String> {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            return if (prefsDir.exists() && prefsDir.isDirectory) {
                prefsDir.list()?.map { it.replace(".xml", "") } ?: emptyList()
            } else {
                emptyList()
            }
        }

        // SharedPreferences 읽기
        internal fun getAllPrefs(context: Context, fileName: String): List<PreferenceItem> {
            val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            val allEntries = prefs.all

            return allEntries.map { (key, value) ->
                val type = when (value) {
                    is String -> PrefType.STRING
                    is Int -> PrefType.INT
                    is Boolean -> PrefType.BOOLEAN
                    is Float -> PrefType.FLOAT
                    is Long -> PrefType.LONG
                    else -> PrefType.UNKNOWN
                }
                PreferenceItem(key, value, type)
            }.sortedBy { it.key } // 가나다순 정렬
        }

        // SharedPreferences 수정
        internal fun updatePref(context: Context, fileName: String, key: String, newValue: Any, type: PrefType) {
            val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            prefs.edit(commit = true) {
                when (type) {
                    PrefType.STRING -> putString(key, newValue as String)
                    PrefType.INT -> putInt(key, newValue.toString().toInt())
                    PrefType.BOOLEAN -> putBoolean(key, newValue as Boolean)
                    PrefType.FLOAT -> putFloat(key, newValue.toString().toFloat())
                    PrefType.LONG -> putLong(key, newValue.toString().toLong())
                    PrefType.UNKNOWN -> {
                        Toast.makeText(context, type.name, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}