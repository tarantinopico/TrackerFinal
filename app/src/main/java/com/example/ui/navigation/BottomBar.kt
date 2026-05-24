package com.example.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.5f else 0.8f)
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0x26000000))
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.verticalGradient(listOf(Color(0x1AFFFFFF), Color.Transparent)))
            .border(1.dp, borderColor, RoundedCornerShape(32.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Routes.DASHBOARD, Icons.Default.Dashboard, "Home", currentRoute, onNavigate)
            NavItem(Routes.LOGGING, Icons.Default.AddCircle, "Log", currentRoute, onNavigate)
            NavItem(Routes.LAB, Icons.Default.Science, "Lab", currentRoute, onNavigate)
            NavItem(Routes.ANALYTICS, Icons.Default.Analytics, "Data", currentRoute, onNavigate)
            NavItem(Routes.SETTINGS, Icons.Default.Settings, "Settings", currentRoute, onNavigate)
        }
    }
}

@Composable
private fun NavItem(
    route: String,
    icon: ImageVector,
    label: String,
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val selected = currentRoute == route
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f)
    )

    Column(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = { onNavigate(route) })
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
        }
    }
}

