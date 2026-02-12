package com.brally.mobile.service.firebase

import android.util.Log
import com.bumptech.glide.load.model.LazyHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

enum class DataResourceType(val domainUrl: String) {
    GITHUB(SeverRemoteConfig.DATA_BASE_URL_GITHUB),
    DATA_OPTIMIZE_URL_BACKUP(SeverRemoteConfig.DATA_OPTIMIZE_URL_BACKUP);
}

object SeverRemoteConfig {

    const val DATA_BASE_URL_GITHUB =  "https://raw.githubusercontent.com/Braly-Danang/cross_stitch_data/main/"
    const val DATA_OPTIMIZE_URL_BACKUP = "https://storage.bralyvn.com/storage-braly/storage-data-app/[DN]%20Cross%20Stitch%20-%20Color%20by%20Number/cross_stitch_data/main/"

    const val URL_README_TEST = DATA_BASE_URL_GITHUB + "README.md"
    private var isGithubAvailable = true

    fun getUrlBase(): String {
        return if (isGithubAvailable) DATA_BASE_URL_GITHUB else DATA_OPTIMIZE_URL_BACKUP
    }

    fun getHeaderToken(): LazyHeaders {
        return LazyHeaders.Builder()
            .addHeader("Accept", "application/vnd.github.v3.raw")
            .addHeader("Authorization", "token ${AppRemoteConfig.getAccessToken()}")
            .build()
    }

    fun isGithubUrl(): Boolean {
        return getUrlBase().contains(DATA_BASE_URL_GITHUB)
    }

    fun setGithubAvailable(isAvailable: Boolean) {
        isGithubAvailable = isAvailable
    }

    suspend fun testReadmeToken(): String? = withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        fun fetchUrl(): String? {
            val request = Request.Builder()
                .url(URL_README_TEST)
                .addHeader("Accept", "application/vnd.github.v3.raw")
                .addHeader("Authorization", "token ${AppRemoteConfig.getAccessToken()}")
                .build()

            return try {
                client.newCall(request).execute().use { response ->
                    setGithubAvailable(response.isSuccessful)
                    if (response.isSuccessful) {
                        response.body?.string()
                    } else {
                        Log.d("CheckToken", "GITHUB TOKEN EXPIRED ---- WARNING")
                        null
                    }
                }
            } catch (e: IOException) {
                null
            }
        }

        var result = fetchUrl()
        return@withContext result
    }
}
