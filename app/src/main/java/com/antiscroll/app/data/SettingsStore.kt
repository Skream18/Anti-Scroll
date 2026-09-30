package com.antiscroll.app.data

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var blockYoutubeShorts: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_YOUTUBE_SHORTS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_YOUTUBE_SHORTS, value).apply()

    var blockInstagramReels: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_INSTAGRAM_REELS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_REELS, value).apply()

    var blockInstagramExplore: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_INSTAGRAM_EXPLORE, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_EXPLORE, value).apply()

    var blockInstagramStories: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_INSTAGRAM_STORIES, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_STORIES, value).apply()

    var darkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    companion object {
        private const val PREFS_NAME = "antiscroll_settings"
        private const val KEY_BLOCK_YOUTUBE_SHORTS = "block_youtube_shorts"
        private const val KEY_BLOCK_INSTAGRAM_REELS = "block_instagram_reels"
        private const val KEY_BLOCK_INSTAGRAM_EXPLORE = "block_instagram_explore"
        private const val KEY_BLOCK_INSTAGRAM_STORIES = "block_instagram_stories"
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
