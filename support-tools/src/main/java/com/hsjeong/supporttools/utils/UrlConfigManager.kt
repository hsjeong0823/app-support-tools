package com.hsjeong.supporttools.utils

import android.content.Context
import com.hsjeong.supporttools.constants.Constants.Preference
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

enum class ServerType {
    DEV,
    STG,
    REAL,
}

data class UrlConfigData(
    @JvmField val scheme: String = "https",
    @JvmField val baseUrl: String,
    @JvmField val targetUrls: Map<ServerType, String>
)

object UrlConfigManager {

    private var serverType: ServerType = ServerType.DEV
    private val hostMap = mutableMapOf<String, UrlConfigData>()
    private val baseUrlMap = mutableMapOf<String, UrlConfigData>()

    @JvmStatic
    fun setUrlConfigData(list: List<UrlConfigData>, addUrlCompleteCallback: (() -> Unit)? = null) {
        hostMap.clear()
        baseUrlMap.clear()
        addUrlConfigData(list, addUrlCompleteCallback)
    }

    @JvmStatic
    fun addUrlConfigData(list: List<UrlConfigData>, addUrlCompleteCallback: (() -> Unit)? = null) {
        list.forEach {
            baseUrlMap[it.baseUrl] = it
            val host = getUtlHost(it.scheme, it.baseUrl) ?: return@forEach
            hostMap[host] = it
        }
        addUrlCompleteCallback?.invoke()
    }

    private fun findUrlConfigData(host: String): UrlConfigData? {
        return hostMap[host]
    }

    @JvmStatic
    fun getUrls(serverType: ServerType): List<String> {
        return hostMap.values.mapNotNull { config ->
            config.targetUrls[serverType]
        }
    }

    @JvmStatic
    fun getTargetUrlsMap(context: Context): Map<String, String> {
        return baseUrlMap.mapValues { (baseUrl, config) ->
            config.targetUrls[getServerType(context)] ?: baseUrl
        }.filterValues { it.isNotEmpty() } // 주소가 비어있는 스펙은 제외
    }

    private fun getUtlHost(scheme: String, baseUrl: String): String? {
        return "$scheme://$baseUrl".toHttpUrlOrNull()?.host
    }

    internal fun setServerType(context: Context, serverType: ServerType) {
        PreferencesUtil.putStringPreferences(context, Preference.KEY_SERVER_TYPE, serverType.name)
    }

    internal fun getServerType(context: Context): ServerType {
        val name = PreferencesUtil.getStringPreferences(context, Preference.KEY_SERVER_TYPE, ServerType.DEV.name)
        return ServerType.valueOf(name!!)
    }

    internal class UrlSwitchingInterceptor(private val context: Context) : Interceptor {
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
