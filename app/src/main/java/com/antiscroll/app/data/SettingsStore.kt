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

    var blockSnapchatSpotlight: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SNAPCHAT_SPOTLIGHT, true)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_SNAPCHAT_SPOTLIGHT, value).apply()

    var blockSnapchatStories: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SNAPCHAT_STORIES, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_SNAPCHAT_STORIES, value).apply()

    var darkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    /**
     * Whether the Scroll Control timer is turned on. Only takes effect while
     * [blockInstagramReels] is off - full block always wins. Toggling this off does NOT
     * reset [scrollControlRemainingMillis] or [scrollControlCooldownUntil]; it only pauses
     * enforcement, so partial progress and any live cooldown survive a toggle.
     */
    var scrollControlEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCROLL_CONTROL_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SCROLL_CONTROL_ENABLED, value).apply()

    /** The configured Reels viewing budget in minutes. Only ever 1, 3, or 5 - enforced by the UI, not here. */
    var scrollControlLimitMinutes: Int
        get() = prefs.getInt(KEY_SCROLL_CONTROL_LIMIT_MINUTES, DEFAULT_SCROLL_CONTROL_LIMIT_MINUTES)
        set(value) = prefs.edit().putInt(KEY_SCROLL_CONTROL_LIMIT_MINUTES, value).apply()

    /**
     * The Reels viewing budget left, in milliseconds. Unlike [scrollControlCooldownUntil],
     * this is a *pausable* duration: it's only decremented while the user is actively
     * watching Reels, not a deadline that keeps counting down regardless. -1L means
     * "never initialized" - callers should treat that as "use the full configured limit".
     * Selecting a new [scrollControlLimitMinutes] always resets this to the new limit.
     */
    var scrollControlRemainingMillis: Long
        get() = prefs.getLong(KEY_SCROLL_CONTROL_REMAINING_MILLIS, -1L)
        set(value) = prefs.edit().putLong(KEY_SCROLL_CONTROL_REMAINING_MILLIS, value).apply()

    /**
     * Absolute epoch-millis timestamp until which Reels is temporarily locked out after the
     * budget above hit zero. Unlike [scrollControlRemainingMillis], this is a
     * *non-pausable* deadline: it counts down in real time no matter what the user does.
     * 0L means "no active cooldown". Selecting a new [scrollControlLimitMinutes] always
     * clears this back to 0.
     */
    var scrollControlCooldownUntil: Long
        get() = prefs.getLong(KEY_SCROLL_CONTROL_COOLDOWN_UNTIL, 0L)
        set(value) = prefs.edit().putLong(KEY_SCROLL_CONTROL_COOLDOWN_UNTIL, value).apply()

    companion object {
        private const val PREFS_NAME = "antiscroll_settings"
        private const val KEY_BLOCK_YOUTUBE_SHORTS = "block_youtube_shorts"
        private const val KEY_BLOCK_INSTAGRAM_REELS = "block_instagram_reels"
        private const val KEY_BLOCK_INSTAGRAM_EXPLORE = "block_instagram_explore"
        private const val KEY_BLOCK_INSTAGRAM_STORIES = "block_instagram_stories"
        private const val KEY_BLOCK_SNAPCHAT_SPOTLIGHT = "block_snapchat_spotlight"
        private const val KEY_BLOCK_SNAPCHAT_STORIES = "block_snapchat_stories"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_SCROLL_CONTROL_ENABLED = "scroll_control_enabled"
        private const val KEY_SCROLL_CONTROL_LIMIT_MINUTES = "scroll_control_limit_minutes"
        private const val KEY_SCROLL_CONTROL_REMAINING_MILLIS = "scroll_control_remaining_millis"
        private const val KEY_SCROLL_CONTROL_COOLDOWN_UNTIL = "scroll_control_cooldown_until"
        private const val DEFAULT_SCROLL_CONTROL_LIMIT_MINUTES = 3
    }
}
