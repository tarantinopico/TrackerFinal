package com.example.data.local.db

import androidx.room.TypeConverter
import org.json.JSONObject

class Converters {
    /** 
     * Validates and converts basic String Maps to JSON and back safely.
     * Prevents adding hefty dependencies just for DB storage of small ratios. 
     */
    @TypeConverter
    fun fromStringMap(map: Map<String, Float>?): String {
        if (map == null) return "{}"
        val json = JSONObject()
        for ((k, v) in map) {
            json.put(k, v.toDouble())
        }
        return json.toString()
    }

    @TypeConverter
    fun toStringMap(jsonString: String?): Map<String, Float> {
        val map = mutableMapOf<String, Float>()
        if (jsonString.isNullOrEmpty()) return map
        try {
            val json = JSONObject(jsonString)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getDouble(key).toFloat()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
}
