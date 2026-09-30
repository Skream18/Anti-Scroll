<div align="center">

# AntiScroll

**Turns off the infinite scroll, leaves everything else alone.**

Blocks YouTube Shorts and Instagram Reels/Explore at the source — no Shorts tab, no Reels
tab, no Explore page to fall into. Stories can be blocked too, as its own separate switch.
Long-form videos and your DMs still work exactly as before. Runs entirely on your phone: no
account, no server, no network access at all.

<br>

[![License: MIT](https://img.shields.io/badge/license-MIT-a3e635?style=flat-square)](LICENSE)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![No tracking](https://img.shields.io/badge/telemetry-none-f472b6?style=flat-square)
![Sideload only](https://img.shields.io/badge/distribution-sideload--only-60a5fa?style=flat-square)

</div>

## Why

I have a doomscrolling problem. Not the "everyone's a little addicted to their phone"
kind — the kind where I'd open YouTube to watch one specific video and resurface forty
minutes later, several Shorts deep, with no memory of choosing to do that. Open Instagram
to reply to one message, end up in the Reels feed instead, every time. Willpower didn't fix
it, because the whole design of those feeds is built to out-stubborn willpower. What
actually worked was removing the door entirely — if the Shorts tab and the Reels tab simply
don't work, there's nothing to slip into.

AntiScroll is that door, closed. I built it for myself, but if you've ever felt that same
slide — open the app for one reason, surface somewhere else entirely with no idea how you
got there — I hope this helps you the way it's helped me.

## Features

- 🚫 **Blocks YouTube Shorts** — however you try to open it: the bottom-nav Shorts tab, or
  a Short thumbnail sitting on the Home feed. Either way, you get bounced straight back.
- 🚫 **Blocks Instagram Reels & Explore** — same deal for both of those tabs, as one
  switch.
- 📵 **Blocks Instagram Stories too — as its own separate switch.** Off by default, since
  plenty of people want Reels/Explore gone but still want to see Stories. Turn it on
  independently if you want those gone as well.
- 💬 **Long-form videos and friends still get through** — none of this touches anything else. Regular YouTube videos play normally, and Instagram DMs are completely untouched...for now.
- 🔛 **A switch for each block, on or off whenever you want** — YouTube Shorts, Instagram
  Reels/Explore, and Instagram Stories are three fully independent toggles. Genuinely need
  one of them for something one day? Flip it off, do what you need, flip it back on.
- 📴 **Fully offline, on-device** — no account, no backend, no network permission in the
  app at all. Nothing that happens in either app is seen, logged, or sent anywhere.

**More features are coming** — this is actively evolving as I keep using it myself and
running into the next thing worth blocking (or the next thing that shouldn't have been).

## Download & install

> [!IMPORTANT]
> **Disable Google Play Protect scanning before you install.** Play Protect will flag this
> APK purely because it isn't from the Play Store, and it can silently block the install or
> push you to uninstall it right after. Turn it off first:
>
> **Play Store → your profile icon (top-right) → Play Protect → Settings icon (top-right) → turn off "Scan apps with Play Protect"**

Then, to get it onto your phone:

1. **[Download the APK](https://github.com/Skream18/Anti-Scroll/releases/download/v1.1.0/AntiScroll-v1.1.0.apk)**
   directly on your phone, or transfer the `.apk` file over from wherever you built or
   received it. (Other versions: [GitHub Releases](https://github.com/Skream18/Anti-Scroll/releases).)
2. **Allow installing from this source.** When you open the file, Android will ask to
   allow installs from whichever app you opened it with (Files, your browser, etc.) — allow
   it, then continue the install.
3. **Open AntiScroll** and tap **Open Accessibility Settings**, find AntiScroll in the
   list, and turn it on.
   - **Android 13+:** if the toggle looks greyed out, that's Android's "Restricted
     settings" protection for apps installed outside the Play Store. Go to
     **Settings → Apps → AntiScroll → ⋮ menu → Allow restricted settings**, then try
     enabling the service again.
4. Back in the app, the status card should turn green ("Service enabled"). Use the three
   switches to turn YouTube Shorts, Instagram Reels/Explore, and Instagram Stories blocking
   on or off independently.

## Building from source

Needs a JDK and the Android SDK — no Android Studio installation required, the Gradle
wrapper handles everything:

```bash
./gradlew assembleDebug
```

The APK comes out at `app/build/outputs/apk/debug/app-debug.apk`.

## How it works

`GuardAccessibilityService` is an [Android accessibility
service](https://developer.android.com/guide/topics/ui/accessibility/service) scoped to
just two packages, `com.google.android.youtube` and `com.instagram.android` (see
`res/xml/accessibility_service_config.xml`). It's a thin dispatcher: every accessibility
event gets routed by package name to that app's own guard.

```
GuardAccessibilityService
 ├─ service/youtube/YouTubeGuard.kt       (everything YouTube-specific)
 └─ service/instagram/InstagramGuard.kt   (everything Instagram-specific)
```

The two guards are fully independent — separate detection rules, separate state, separate
files — so a change or a bug on one side can't affect the other. Each one watches for two
things:

- **A tap on the blocked tab** (Shorts / Reels / Explore) — caught as a one-shot click
  event, since polling whether a tab is "currently selected" turns out to be unreliable
  (it can stay looking selected long after you've actually left).
- **The blocked screen actually appearing on its own** — e.g. a Short opened from a
  thumbnail on the Home feed rather than the tab, or a Story (which has no tab at all —
  it's opened by tapping a profile's story ring, so this is its only signal). This matches
  known resource IDs (`reel_player_page_container` for Shorts, `clips_viewer_view_pager`
  for Reels, `reel_viewer_root` for Stories — all taken from
  [Scrolless](https://github.com/duartebarbosadev/Scrolless), another open-source
  accessibility blocker) filtered down to only nodes that are actually visible and filling
  the screen right now, not a video pager's off-screen cached page.

Reels/Explore and Stories are two entirely independent blocks even though they're both
Instagram - separate toggles, separate detection, separate internal state - specifically
because an earlier, broader version of the Reels detector also matched Stories' resource ID
by accident (both happen to contain "reel") and blocked them as an unwanted side effect.

Either signal presses the system **Back** action once to leave the screen. Re-arming for
the next attempt happens on a short fixed timer rather than waiting for the screen to
"look" unblocked again — a transient mid-animation reading isn't reliable enough to trust,
and acting on it risked sending a second, unwanted Back press.

## Known caveats

- **Apps update, IDs drift.** Instagram and YouTube change resource IDs and layouts over
  time, and some IDs are obfuscated. Expect to need occasional tweaks in `YouTubeGuard.kt`
  / `InstagramGuard.kt` after an app update changes something.
- **The service can be turned off any time** in Settings — there's no lock or Device Admin
  gate (yet).
- **Reels opened from an Instagram DM aren't blocked.** That's intentional for now — a DM
  reel doesn't select the Reels tab, so it's left alone rather than guessed at. Stopping it
  from endlessly auto-advancing is planned for a later version.
- **No Play Store distribution.** This is built and sideloaded for personal use;
  publishing to Google Play would additionally require an in-app accessibility-use
  disclosure and a declaration form.

## License

[MIT](LICENSE).
