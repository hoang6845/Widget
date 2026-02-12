package com.app.base.session

import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.utils.gsonStrToList
import com.brally.mobile.utils.pref.LocalCache

private const val KEY_FIRST_TUTORIAL = "first_tutorial"

fun setFirstTutorial(isFirst: Boolean) {
    LocalCache.getInstance().put(KEY_FIRST_TUTORIAL, isFirst)
}

fun isFirstTutorial(): Boolean {
    return LocalCache.getInstance().getData(KEY_FIRST_TUTORIAL, Boolean::class.java) ?: true
}


inline fun <reified T> getLocalData(): List<T> {
    val json = LocalCache.getInstance().getData(T::class.java.name, String::class.java)
    if (json.isNullOrEmpty()) return emptyList()
    val list = gsonStrToList(json, T::class.java)
    return list
}
