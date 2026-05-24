package com.example.data.local.db

import androidx.room.TypeConverter
import org.json.JSONObject

class Converters {
    /** 
     * Validates and converts basic String Maps to JSON and back safely.
     * Prevents adding hefty dependencies just for DB storage of small ratios. 
     */
    @TypeConverter
    fun fromStringDoubleMap(map: Map<String, Double>?): String {
        if (map == null) return "{}"
        val json = JSONObject()
        for ((k, v) in map) {
            json.put(k, v)
        }
        return json.toString()
    }

    @TypeConverter
    fun toStringDoubleMap(jsonString: String?): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        if (jsonString.isNullOrEmpty()) return map
        try {
            val json = JSONObject(jsonString)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = json.getDouble(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
}
