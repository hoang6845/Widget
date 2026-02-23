package com.brally.mobile.service.firebase

import com.brally.mobile.base.application.appInfo
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.data.model.CommonAppConfig
import com.brally.mobile.utils.Constant
import com.brally.mobile.utils.Constant.DEFAULT_ITEM_HOME_ORDER
import com.brally.mobile.utils.gsonStrToList
import com.brally.mobile.utils.readAssetsFile
import com.brally.mobile.utils.strToObj
import com.bralydn.analytics.config.BralyDNRemoteConfigImpl
import com.google.gson.JsonArray
import com.google.gson.JsonParser

object AppRemoteConfig {
    private val braly by lazy { BralyDNRemoteConfigImpl() }

    /*************** KEY ************************/
    private const val ACCESS_KEY_REMOTE_DATA = "access_key_remote_data"
    private const val KEY_SHOW_CMP = "show_cmp"
    private const val DATA_PIXEL_ART = "data_pixel_art"
    private const val DATA_CATEGORIES = "data_categories_pixel_art"
    private val KEY_ORDER_ITEM_HOME = "order_item_home_glow_dot_art"
    private const val KEY_COMMON_APP_CONFIG = "common_app_config"

    /*******************************************/

    fun getAccessToken(): String {
        return braly.getString(ACCESS_KEY_REMOTE_DATA) ?: ""
    }

    fun getShowCmp(): Boolean {
        return braly.getBoolean(KEY_SHOW_CMP) == true
    }

    fun getCommonAppConfig(): CommonAppConfig {
        return try {
            braly.getString(KEY_COMMON_APP_CONFIG)?.let { json ->
                return strToObj(json, CommonAppConfig::class.java) ?: CommonAppConfig()
            } ?: CommonAppConfig()
        } catch (e: Exception) {
            e.printStackTrace()
            CommonAppConfig()
        }
    }

    fun getOrderItemHome(): String {
        return braly.getString(KEY_ORDER_ITEM_HOME) ?: DEFAULT_ITEM_HOME_ORDER
    }

    fun <T> getListData(key: String, claszz: Class<T>): List<T> {
        return try {
            val json = braly.getString(key)?.ifEmpty {
                getBaseApplication().assets.readAssetsFile("$key.json")
            }
            gsonStrToList(json, claszz)

        } catch (e: Exception) {
            e.printStackTrace()
//            emptyList()
           if (appInfo().isDebug) {
               val json = getBaseApplication().assets.readAssetsFile("$key.json")
               gsonStrToList(json, claszz)
           } else {
                emptyList()
           }
        }
    }

    fun <T> getData(key: String, claszz: Class<T>): T? {
        return try {
            val json = braly.getString(key)?.ifEmpty {
                getBaseApplication().assets.readAssetsFile("$key.json")
            }
            strToObj(json, claszz)

        } catch (e: Exception) {
            e.printStackTrace()
//            emptyList()
            if (appInfo().isDebug) {
                val json = getBaseApplication().assets.readAssetsFile("$key.json")
                strToObj(json, claszz)
            } else {
                null
            }
        }
    }

    fun getListArt(): List<ArtItem> {
        val listPathArtItem = getListData(DATA_PIXEL_ART, ArtItem::class.java)
        return listPathArtItem
    }

    fun getListCategory(languageKey: String): List<CategoryItem> {
        return try {
            val json = braly.getString(DATA_CATEGORIES)?.ifEmpty {
                getBaseApplication().assets.readAssetsFile("$DATA_CATEGORIES.json")
            }
            json?.let { convertJsonToCategoryList(it, languageKey) } ?: emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
//            emptyList()
            if (appInfo().isDebug) {
                val json = getBaseApplication().assets.readAssetsFile("$DATA_CATEGORIES.json")
                convertJsonToCategoryList(json, languageKey)
            } else {
                emptyList()
            }
        }
    }

    fun convertJsonToCategoryList(jsonString: String, langKey: String): List<CategoryItem> {
        val jsonArray: JsonArray = JsonParser.parseString(jsonString).asJsonArray

        return jsonArray.map { jsonElement ->
            val obj = jsonElement.asJsonObject
            val type = obj.get("type")?.asString ?: ""
            val value = obj.get(langKey)?.asString ?: ""
            CategoryItem(type = type, value = value)
        }
    }
}
