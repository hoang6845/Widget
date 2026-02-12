package com.brally.mobile.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ArtItem(
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("level")
    var level: Int = 1,
    @SerializedName("category")
    var category: String = "",
    @SerializedName("order")
    var order: Int = 1,
    @SerializedName("ratio")
    var ratio: List<Int> = mutableListOf<Int>(1, 1),
    @SerializedName("scale")
    var scale: Double = 1.0,
    @SerializedName("limit_color")
    var limitColor: Int = 10,
    @SerializedName("image_path")
    var imagePath: String = "",

    var isCompleted: Boolean = false,
) : Parcelable {

    fun getImagePathForLevel(level: Int): String {
        val stringReplace = "hard"
        val newString = when (level) {
            5 -> "easy"
            10 -> "normal"
            15 -> "hard"
            else -> stringReplace
        }
        return imagePath.replace(stringReplace, newString)
    }
}
