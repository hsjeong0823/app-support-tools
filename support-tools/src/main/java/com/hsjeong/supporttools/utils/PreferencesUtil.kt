package com.hsjeong.supporttools.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.hsjeong.supporttools.constants.Constants.Preference

class PreferencesUtil() {
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Preference.PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun putBooleanPreferences(context: Context, key: String, value: Boolean) {
        getSharedPreferences(context).edit(true) { putBoolean(key, value) }
    }

    fun putStringPreferences(context: Context, key: String, value: String) {
        getSharedPreferences(context).edit(true) { putString(key, value) }
    }

    fun getBooleanPreferences(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }

    fun getStringPreferences(context: Context, key: String, defaultValue: String? = null): String? {
        return getSharedPreferences(context).getString(key, defaultValue)
    }

    fun setScreenNameOverLayEnable(context: Context, boolean: Boolean) {
        PreferencesUtil().putBooleanPreferences(context, Preference.KEY_SCREEN_NAME_OVERLAY_ENABLE, boolean)
    }

    fun getScreenNameOverLayEnable(context: Context): Boolean {
        return PreferencesUtil().getBooleanPreferences(context, Preference.KEY_SCREEN_NAME_OVERLAY_ENABLE, true)
    }

    fun setNetworkLogEnable(context: Context, boolean: Boolean) {
        PreferencesUtil().putBooleanPreferences(context, Preference.KEY_NETWORK_LOG_ENABLE, boolean)
    }

    fun getNetworkLogEnable(context: Context): Boolean {
        return PreferencesUtil().getBooleanPreferences(context, Preference.KEY_NETWORK_LOG_ENABLE, true)
    }

    fun setUrlSwitchingEnable(context: Context, boolean: Boolean) {
        PreferencesUtil().putBooleanPreferences(context, Preference.KEY_URL_SWITCHING_ENABLE, boolean)
    }

    fun getUrlSwitchingEnable(context: Context): Boolean {
        return PreferencesUtil().getBooleanPreferences(context, Preference.KEY_URL_SWITCHING_ENABLE, true)
    }
}