package com.yusufozturk.cinetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufozturk.cinetrack.data.local.NotificationPreferences
import com.yusufozturk.cinetrack.ui.theme.FlicksRed

@Composable
fun NotificationSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { NotificationPreferences(context) }

    var newReleases by remember { mutableStateOf(prefs.newReleasesEnabled) }
    var watchlistReminders by remember { mutableStateOf(prefs.watchlistRemindersEnabled) }
    var trailerAlerts by remember { mutableStateOf(prefs.trailerAlertsEnabled) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Notifications",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            NotificationToggleRow(
                title = "New Releases",
                subtitle = "Get notified when new movies are added",
                checked = newReleases,
                onCheckedChange = {
                    newReleases = it
                    prefs.newReleasesEnabled = it
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationToggleRow(
                title = "Watchlist Reminders",
                subtitle = "Reminders for movies in your watchlist",
                checked = watchlistReminders,
                onCheckedChange = {
                    watchlistReminders = it
                    prefs.watchlistRemindersEnabled = it
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotificationToggleRow(
                title = "Trailer Alerts",
                subtitle = "Notify when a trailer becomes available",
                checked = trailerAlerts,
                onCheckedChange = {
                    trailerAlerts = it
                    prefs.trailerAlertsEnabled = it
                }
            )
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = FlicksRed
            )
        )
    }
}
