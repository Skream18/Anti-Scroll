package com.antiscroll.app.data

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var blockYoutubeShorts: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_YOUTUBE_SHORTS, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_YOUTUBE_SHORTS, value).apply()

    var blockInstagramReelsExplore: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_INSTAGRAM_REELS_EXPLORE, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_REELS_EXPLORE, value).apply()

    var blockInstagramStories: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_INSTAGRAM_STORIES, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_INSTAGRAM_STORIES, value).apply()

    companion object {
        private const val PREFS_NAME = "antiscroll_settings"
        private const val KEY_BLOCK_YOUTUBE_SHORTS = "block_youtube_shorts"
        private const val KEY_BLOCK_INSTAGRAM_REELS_EXPLORE = "block_instagram_reels_explore"
        private const val KEY_BLOCK_INSTAGRAM_STORIES = "block_instagram_stories"
    }
}
