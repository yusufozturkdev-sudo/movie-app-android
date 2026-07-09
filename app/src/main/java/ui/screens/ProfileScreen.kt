package com.yusufozturk.cinetrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yusufozturk.cinetrack.BuildConfig
import com.yusufozturk.cinetrack.ui.theme.FlicksRed
import com.yusufozturk.cinetrack.ui.theme.FlicksSurface
import com.yusufozturk.cinetrack.ui.theme.FlicksTextSecondary

@Composable
fun ProfileScreen(
    watchlistCount: Int,
    ratedCount: Int,
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "CINETRACK",
            color = FlicksRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(FlicksRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = "Yusuf Öztürk", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Movie Enthusiast", color = FlicksTextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- TMDB Bağlantı Durumu ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FlicksSurface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = null,
                tint = if (isLoggedIn) Color(0xFF4CAF50) else FlicksTextSecondary
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = if (isLoggedIn) "Connected to TMDB" else "Not connected",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isLoggedIn) "Your watchlist syncs with your TMDB account"
                    else "Log in to sync your watchlist across devices",
                    color = FlicksTextSecondary,
                    fontSize = 12.sp
                )
            }
            if (isLoggedIn) {
                TextButton(onClick = onLogoutClick) {
                    Text("Log out", color = FlicksRed)
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = FlicksRed)
                ) {
                    Text("Log in")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FlicksSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(label = "Watchlist", value = watchlistCount.toString())
            ProfileStat(label = "Watched", value = "0")
            ProfileStat(label = "Reviews", value = ratedCount.toString())
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ProfileMenuItem(icon = Icons.Default.Settings, title = "Account Settings", onClick = { })
        ProfileMenuItem(icon = Icons.Default.Notifications, title = "Notifications", onClick = { })
        ProfileMenuItem(icon = Icons.Default.Info, title = "About", onClick = { showAboutDialog = true })
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FlicksRed)
            }
        },
        title = {
            Text(text = "CineTrack", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    color = FlicksTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "CineTrack is a movie discovery app that lets you browse popular titles, " +
                            "search by name, explore genres, and keep track of what you want to watch.",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Built by Yusuf Öztürk using Kotlin & Jetpack Compose.",
                    color = FlicksTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                    color = FlicksTextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = FlicksSurface
    )
}

@Composable
private fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = FlicksTextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FlicksSurface)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = FlicksTextSecondary)
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 12.dp).weight(1f)
        )
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = FlicksTextSecondary)
    }
    Spacer(modifier = Modifier.height(8.dp))
}