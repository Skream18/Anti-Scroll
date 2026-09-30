package com.antiscroll.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antiscroll.app.R

/**
 * A supported UI language. [tag] is a BCP-47 language tag passed to
 * AppCompatDelegate.setApplicationLocales(), or null for "follow the system language".
 * [nativeName] is deliberately NOT a translated string resource: a language picker
 * conventionally lists every option in its own native name (e.g. "Français" stays
 * "Français" even when the current UI language is English) so a user can find their
 * language regardless of what the app currently displays.
 */
data class AppLanguage(val tag: String?, val nativeName: String)

val SUPPORTED_LANGUAGES = listOf(
    AppLanguage(tag = "en", nativeName = "English"),
    AppLanguage(tag = "fr", nativeName = "Français"),
    AppLanguage(tag = "es", nativeName = "Español"),
    AppLanguage(tag = "de", nativeName = "Deutsch"),
    AppLanguage(tag = "pt", nativeName = "Português"),
    AppLanguage(tag = "zh", nativeName = "中文（简体）"),
    AppLanguage(tag = "zh-HK", nativeName = "中文（繁體，香港）"),
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    darkTheme: Boolean,
    onSelectDarkTheme: (Boolean) -> Unit,
    currentLanguageTag: String?,
    onSelectLanguage: (String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            // weight(1f) keeps the title from ever overflowing the row's bounds on a
            // narrow screen, same fix as HomeScreen's header - see its comment for why.
            Text(
                text = stringResource(R.string.settings_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).padding(start = 4.dp),
            )
        }

        Text(
            text = stringResource(R.string.settings_theme_header),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        SettingsOptionRow(
            title = stringResource(R.string.settings_theme_dark),
            selected = darkTheme,
            onClick = { onSelectDarkTheme(true) },
        )
        SettingsOptionRow(
            title = stringResource(R.string.settings_theme_light),
            selected = !darkTheme,
            onClick = { onSelectDarkTheme(false) },
            modifier = Modifier.padding(top = 8.dp),
        )

        Text(
            text = stringResource(R.string.settings_language_header),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        SettingsOptionRow(
            title = stringResource(R.string.settings_language_system_default),
            selected = currentLanguageTag == null,
            onClick = { onSelectLanguage(null) },
        )
        SUPPORTED_LANGUAGES.forEach { language ->
            SettingsOptionRow(
                title = language.nativeName,
                selected = currentLanguageTag == language.tag,
                onClick = { onSelectLanguage(language.tag) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
internal fun SettingsOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // weight(1f) so a long language name (e.g. "中文（繁體，香港）") can't push the
        // checkmark off the row - same fix as HomeScreen's header, see its comment.
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.cd_selected),
                tint = AntiScrollGreen,
            )
        }
    }
}
