package com.antiscroll.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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

@Composable
fun ScrollControlScreen(
    onOpenSettings: () -> Unit,
    fullBlockEnabled: Boolean,
    scrollControlEnabled: Boolean,
    onToggleScrollControlEnabled: (Boolean) -> Unit,
    scrollControlLimitMinutes: Int,
    onSelectLimitMinutes: (Int) -> Unit,
    remainingMillis: Long,
    cooldownUntil: Long,
) {
    val controlsEnabled = !fullBlockEnabled
    val now = System.currentTimeMillis()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.scroll_control_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (fullBlockEnabled) {
            Text(
                text = stringResource(R.string.scroll_control_disabled_by_full_block),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
            )
        }

        ToggleRow(
            title = stringResource(R.string.scroll_control_enable_toggle),
            checked = scrollControlEnabled,
            onCheckedChange = onToggleScrollControlEnabled,
            enabled = controlsEnabled,
            modifier = Modifier.padding(top = 24.dp),
        )

        Text(
            text = stringResource(R.string.scroll_control_limit_header),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        val rowsEnabled = controlsEnabled && scrollControlEnabled
        SettingsOptionRow(
            title = stringResource(R.string.scroll_control_limit_1min),
            selected = scrollControlLimitMinutes == 1,
            onClick = { onSelectLimitMinutes(1) },
            enabled = rowsEnabled,
        )
        SettingsOptionRow(
            title = stringResource(R.string.scroll_control_limit_3min),
            selected = scrollControlLimitMinutes == 3,
            onClick = { onSelectLimitMinutes(3) },
            enabled = rowsEnabled,
            modifier = Modifier.padding(top = 8.dp),
        )
        SettingsOptionRow(
            title = stringResource(R.string.scroll_control_limit_5min),
            selected = scrollControlLimitMinutes == 5,
            onClick = { onSelectLimitMinutes(5) },
            enabled = rowsEnabled,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (controlsEnabled && scrollControlEnabled) {
            val displayRemainingMillis = if (remainingMillis < 0L) {
                scrollControlLimitMinutes * 60_000L
            } else {
                remainingMillis
            }
            val statusText = if (cooldownUntil > now) {
                stringResource(R.string.scroll_control_cooldown_status, formatMillis(cooldownUntil - now))
            } else {
                stringResource(R.string.scroll_control_remaining_status, formatMillis(displayRemainingMillis.coerceAtLeast(0)))
            }
            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
