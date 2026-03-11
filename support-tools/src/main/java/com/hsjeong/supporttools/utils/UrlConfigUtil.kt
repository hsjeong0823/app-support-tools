package com.hsjeong.supporttools.utils

import android.content.Context
import com.hsjeong.supporttools.constants.Constants.Preference
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

object UrlConfigUtil {
    enum class ServerType {
        DEV,
        STG,
        REAL
    }

    data class UrlConfigData(
        @JvmField val scheme: String = "https",
        @JvmField val baseUrl: String,
        @JvmField val targetUrls: Map<ServerType, String>
    )

    private var serverType: ServerType = ServerType.DEV
    private val hostMap = mutableMapOf<String, UrlConfigData>()

    @JvmStatic
    fun setUrlConfigData(list: List<UrlConfigData>) {
        hostMap.clear()
        addUrlConfigData(list)
    }

    @JvmStatic
    fun addUrlConfigData(list: List<UrlConfigData>) {
        list.forEach {
            val host = "${it.scheme}://${it.baseUrl}".toHttpUrlOrNull()?.host ?: return@forEach
            hostMap[host] = it
        }
    }

    private fun findUrlConfigData(host: String): UrlConfigData? {
        return hostMap[host]
    }

    fun getUrls(serverType: ServerType): List<String> {
        return hostMap.values.mapNotNull { config ->
            config.targetUrls[serverType]
        }
    }

    fun setServerType(context: Context, serverType: ServerType) {
        PreferencesUtil.putStringPreferences(context, Preference.KEY_SERVER_TYPE, serverType.name)
    }

    fun getServerType(context: Context): ServerType {
        val name = PreferencesUtil.getStringPreferences(context, Preference.KEY_SERVER_TYPE, ServerType.DEV.name)
        return ServerType.valueOf(name!!)
    }

    class UrlSwitchingInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val originalUrl = request.url
            val urlConfigData = findUrlConfigData(originalUrl.host)

            val isNetworkSwitching = PreferencesUtil.getUrlSwitchingEnable(context)
            if (!isNetworkSwitching) {
                return chain.proceed(request)
            }

            if (urlConfigData != null) {
                val target = urlConfigData.targetUrls[getServerType(context)]
                if (target != null) {
                    val newUrl = originalUrl.toString().replace(urlConfigData.baseUrl, target)
                    val newRequest = request.newBuilder()
                        .url(newUrl)
                        .build()
                    return chain.proceed(newRequest)
                }
            }
            return chain.proceed(request)
        }
    }
}