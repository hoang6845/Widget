package com.app.base.utils

import android.content.Context
import com.app.base.model.AnimationData
import com.app.base.model.Design
import com.app.base.model.KeyFrame
import com.app.base.model.Photo
import com.app.base.model.Rect
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Tiện ích load AnimationData từ JSON (exported từ After Effects script).
 *
 * Format JSON mẫu:
 * {
 *   "design": { "width": 1080, "height": 1920 },
 *   "photos": {
 *     "PHOTO_1": {
 *       "rect": { "x": 0.05, "y": 0.1, "w": 0.4, "h": 0.35 },
 *       "keys": [
 *         { "time": 0, "x": 0.25, "y": 0.275, "scale": 1.0, "rotation": 0.0 },
 *         ...
 *       ]
 *     }
 *   }
 * }
 */
object AnimationDataLoader {

    private val gson = Gson()

    /**
     * Load từ file trong assets folder (ví dụ: "animation_data.json")
     */
    fun loadFromAssets(context: Context, fileName: String): AnimationData? {
        return try {
            val json = context.assets.open(fileName).bufferedReader().readText()
            fromJson(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Load từ JSON string trực tiếp
     */
    fun fromJson(json: String): AnimationData? {
        return try {
            val root = gson.fromJson(json, JsonObject::class.java)

            // Parse design
            val designObj = root.getAsJsonObject("design")
            val design = Design(
                width = designObj.get("width").asInt,
                height = designObj.get("height").asInt
            )

            // Parse photos
            val photosObj = root.getAsJsonObject("photos")
            val photos = mutableMapOf<String, Photo>()

            for ((key, photoElement) in photosObj.entrySet()) {
                val photoObj = photoElement.asJsonObject

                // Parse rect
                val rectObj = photoObj.getAsJsonObject("rect")
                val rect = Rect(
                    x = rectObj.get("x").asDouble,
                    y = rectObj.get("y").asDouble,
                    w = rectObj.get("w").asDouble,
                    h = rectObj.get("h").asDouble
                )

                // Parse keyframes
                val keysArray = photoObj.getAsJsonArray("keys")
                val keys = keysArray.map { keyElement ->
                    val keyObj = keyElement.asJsonObject
                    KeyFrame(
                        time = keyObj.get("time").asLong,
                        x = keyObj.get("x").asDouble,
                        y = keyObj.get("y").asDouble,
                        scale = keyObj.get("scale").asDouble,
                        rotation = keyObj.get("rotation").asDouble
                    )
                }

                photos[key] = Photo(rect = rect, keys = keys.sortedBy { it.time })
            }

            AnimationData(design = design, photos = photos)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Xuất AnimationData thành JSON (dùng để debug / test)
     */
    fun toJson(data: AnimationData): String {
        return gson.toJson(data)
    }
}