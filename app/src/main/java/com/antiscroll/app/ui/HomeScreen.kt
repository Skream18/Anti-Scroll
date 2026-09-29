package com.antiscroll.app.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    serviceEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    blockYoutubeShorts: Boolean,
    onToggleBlockYoutubeShorts: (Boolean) -> Unit,
    blockInstagramReelsExplore: Boolean,
    onToggleBlockInstagramReelsExplore: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AntiScrollBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "AntiScroll",
            color = AntiScrollTextPrimary,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Block Shorts. Block Reels. Keep scrolling out.",
            color = AntiScrollTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )

        StatusCard(
            serviceEnabled = serviceEnabled,
            onOpenAccessibilitySettings = onOpenAccessibilitySettings,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )

        Text(
            text = "BLOCKS",
            color = AntiScrollTextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp),
        )
        ToggleRow(
            title = "Block YouTube Shorts",
            checked = blockYoutubeShorts,
            onCheckedChange = onToggleBlockYoutubeShorts,
        )
        ToggleRow(
            title = "Block Instagram Reels & Explore",
            checked = blockInstagramReelsExplore,
            onCheckedChange = onToggleBlockInstagramReelsExplore,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = AntiScrollBorder, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = AntiScrollTextPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )
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
            .border(width = 1.dp, color = AntiScrollBorder, shape = RoundedCornerShape(12.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (serviceEnabled) AntiScrollGreen else AntiScrollTextSecondary,
                        shape = CircleShape,
                    ),
            )
            Text(
                text = if (serviceEnabled) "Service enabled" else "Service disabled",
                color = AntiScrollTextPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
        Text(
            text = if (serviceEnabled) {
                "AntiScroll is watching YouTube and Instagram."
            } else {
                "Turn on the accessibility service to start blocking."
            },
            color = AntiScrollTextSecondary,
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
                Text("Open Accessibility Settings")
            }
        }
    }
}
