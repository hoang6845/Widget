package com.brally.mobile.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.bralydn.ads.R

@Keep
enum class AppLanguage(
    @DrawableRes val imageRes: Int,
    val countryCode: String,
    val countryName: String,
) {
    ENGLISH(R.drawable.flag_uk, "en", "English"), SPANISH(
        R.drawable.flag_es,
        "es",
        "Español"
    ),
    FRANCAIS(R.drawable.flag_fr, "fr", "Français"), HINDI(
        R.drawable.flag_hi,
        "hi",
        "Hindi"
    ),
    INDONESIA(R.drawable.flag_id, "in", "Indonesia"), PORTUGAL(
        R.drawable.flag_pt,
        "pt",
        "Portuguese"
    ),
    ROMANIA(R.drawable.flag_ro, "ro", "România"), DEUTSCH(
        R.drawable.flag_de,
        "de",
        "Deutsch"
    ),
    ITALIA(R.drawable.flag_it, "it", "Italiano"), NETHERLANDS(
        R.drawable.flag_ne,
        "nl",
        "Nederland"
    ),
    VIETNAMESE(R.drawable.flag_vi, "vi", "Vietnamese"), RUSSIA(
        R.drawable.flag_ru,
        "ru",
        "Russian"
    ),
    OTHER(R.drawable.ic_other_common, "other", "Other");

    companion object {

        fun getEntries() = AppLanguage.entries.filter { it != OTHER }

        fun getLanguagePosition(languageCode: String): Int {
            val language = AppLanguage.entries.find { it.countryCode == languageCode } ?: ENGLISH
            return AppLanguage.entries.indexOf(language)
        }

        fun getLanguageByPosition(position: Int): List<AppLanguageSelector> {
            val languages = entries.toList()
            val languageSelector = languages.mapIndexed { index, language ->
                AppLanguageSelector(language, isCheck = index == position)
            }
            return languageSelector
        }
    }
}

@Keep
data class AppLanguageSelector(
    val language: AppLanguage,
    var isCheck: Boolean = false,
)
