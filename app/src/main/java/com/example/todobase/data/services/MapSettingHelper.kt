package com.example.todobase.data.services

import android.content.Context
import com.example.todobase.BuildConfig

enum class MapStyleOptions(val title: String, val styleUrlName: String, val previewImageUrl: String = "https://cloud.maptiler.com/static/img/maps/$styleUrlName.png") {
    STREETS("Streets", "streets-v4"),
    OUTDOORS("Outdoors", "outdoor-v2"),
    SATELLITE("Satellite", "hybrid"),
}

object MapSettingHelper {
    private const val PREF_NAME = "MapSettings"
    private const val KEY_MAP_STYLE = "map_style"
    
    fun saveMapStyle(context: Context, style: MapStyleOptions) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MAP_STYLE, style.styleUrlName).apply()
    }
    
    fun getMapStyle(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val styleName = prefs.getString(KEY_MAP_STYLE, MapStyleOptions.STREETS.styleUrlName)
        val apiKey = BuildConfig.API_KEY
        
        return "https://api.maptiler.com/maps/$styleName/style.json?key=$apiKey"
    }
    
    fun getSelectedOption(context: Context): MapStyleOptions {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val styleName = prefs.getString(KEY_MAP_STYLE, MapStyleOptions.STREETS.styleUrlName)
        return MapStyleOptions.entries.find { it.styleUrlName == styleName } ?: MapStyleOptions.STREETS
    }
}