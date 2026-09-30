package com.antiscroll.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.antiscroll.app.R

@Composable
fun HomeScreen(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenSettings: () -> Unit,
    blockYoutubeShorts: Boolean,
    onToggleBlockYoutubeShorts: (Boolean) -> Unit,
    blockInstagramReels: Boolean,
    onToggleBlockInstagramReels: (Boolean) -> Unit,
    blockInstagramExplore: Boolean,
    onToggleBlockInstagramExplore: (Boolean) -> Unit,
    blockInstagramStories: Boolean,
    onToggleBlockInstagramStories: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // weight(1f) caps this column at the space left after the settings button's own
            // fixed size, instead of growing with the text and pushing that button off the
            // edge of the screen - which is exactly what happened with longer translations
            // (French/Spanish/German/Portuguese all run longer than English here) before
            // this was added.
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.home_tagline),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        StatusCard(
            serviceEnabled = serviceEnabled,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )

        Text(
            text = stringResource(R.string.home_blocks_header),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
        )
        ToggleRow(
            title = stringResource(R.string.home_block_youtube_shorts),
            checked = blockYoutubeShorts,
            onCheckedChange = onToggleBlockYoutubeShorts,
            icon = R.drawable.ic_app_youtube,
        )
        ToggleRow(
            title = stringResource(R.string.home_block_instagram_reels),
            checked = blockInstagramReels,
            onCheckedChange = onToggleBlockInstagramReels,
            modifier = Modifier.padding(top = 8.dp),
            icon = R.drawable.ic_app_instagram,
        )
        ToggleRow(
            title = stringResource(R.string.home_block_instagram_explore),
            checked = blockInstagramExplore,
            onCheckedChange = onToggleBlockInstagramExplore,
            modifier = Modifier.padding(top = 8.dp),
            icon = R.drawable.ic_app_instagram,
        )
        ToggleRow(
            title = stringResource(R.string.home_block_instagram_stories),
            checked = blockInstagramStories,
            onCheckedChange = onToggleBlockInstagramStories,
            modifier = Modifier.padding(top = 8.dp),
            icon = R.drawable.ic_app_instagram,
        )
    }
}

@Composable
internal fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: Int? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // weight(1f) here for the same reason as HomeScreen's header: without it, a long
        // enough translated title can grow past the available width and push the
        // fixed-size Switch off the edge of the row instead of just wrapping.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            } else {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AntiScrollGreen,
                checkedThumbColor = androidx.compose.ui.graphics.Color.Black,
            ),
        )
    }
}

@Composable
private fun StatusCard(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (serviceEnabled) AntiScrollGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape,
                    ),
            )
            Text(
                text = if (serviceEnabled) stringResource(R.string.home_status_enabled) else stringResource(R.string.home_status_disabled),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Text(
            text = if (serviceEnabled) {
                stringResource(R.string.home_status_description_enabled)
            } else {
                stringResource(R.string.home_status_description_disabled)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
        )
        if (!serviceEnabled) {
            Button(
                onClick = onOpenAccessibilitySettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AntiScrollGreen,
                    contentColor = androidx.compose.ui.graphics.Color.Black,
                ),
            ) {
                Text(stringResource(R.string.home_open_accessibility_settings))
            }
        }
    }
}
