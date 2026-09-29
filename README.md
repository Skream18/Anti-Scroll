# AntiScroll

A minimal Android accessibility service that blocks YouTube Shorts and the Instagram
Reels/Explore tabs. Everything runs on-device - no accounts, no network access, no backend.

## What it does (v1)

- **YouTube:** blocks the Shorts player, however it's opened (the bottom-nav Shorts tab,
  or a Short opened from a Home-feed thumbnail/shared link).
- **Instagram:** blocks the bottom-nav **Reels** tab and the **Explore/Search** tab.
- Each block has its own on/off toggle in the app.
- A reel opened from an Instagram DM thread is **not** blocked in v1 (deferred to v2,
  along with stopping it from auto-advancing to the next reel).

## Installing

1. Enable **Settings → Security → Install unknown apps** for whichever app you use to
   open the APK file (Files, a browser, etc.).
2. Install the APK and open AntiScroll.
3. Tap **Open Accessibility Settings**, find AntiScroll in the list, and turn it on.
   - **Android 13+:** if the toggle is greyed out, this is Android's "Restricted
     settings" protection for apps installed outside the Play Store. Go to
     **Settings → Apps → AntiScroll → (⋮ menu) → Allow restricted settings**, then try
     enabling the service again.
4. Back in the app, the status card should show a green dot ("Service enabled").

## Building from source

Requires a JDK and the Android SDK (see `local.properties` for the SDK path used on this
machine). No Android Studio installation is required - the Gradle wrapper handles
everything:

```
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## How detection works

`GuardAccessibilityService` listens only to `com.google.android.youtube` and
`com.instagram.android` (see `res/xml/accessibility_service_config.xml`). On each screen
change it inspects the accessibility node tree (`YouTubeDetector` /
`InstagramDetector`) for signals like resource IDs, content descriptions, and selected
tab state, and presses the system Back action when a blocked screen is found.

Blocking is edge-triggered: once a block fires for a given screen, it won't fire again
until the tree shows a non-blocked screen. This matters because a single screen
transition fires many rapid accessibility events while it animates in - without this
guard, multiple Back presses stack up and can overshoot past the intended screen (e.g.
exiting the app entirely) instead of landing cleanly on the one underneath.

## Known caveats

- **Apps update, IDs drift.** Instagram and YouTube change resource IDs and layouts
  over time, and some IDs are obfuscated. Content-description and structural matches
  (like "is this node filling the screen") tend to be more stable than exact IDs, but
  expect to need occasional tweaks in `YouTubeDetector.kt` / `InstagramDetector.kt`
  after an app update breaks something.
- **The service can be turned off any time** in Settings - there's no lock/Device-Admin
  gate in v1.
- **No Play Store distribution.** This is built and installed directly (sideloaded) for
  personal use; publishing to Google Play would additionally require an in-app
  accessibility-use disclosure and a declaration form.

## License

MIT.
