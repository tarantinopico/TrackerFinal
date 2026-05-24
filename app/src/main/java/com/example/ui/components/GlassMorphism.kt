package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)
    val gradientStart = if (isDark) Color(0x1AFFFFFF) else Color(0x66FFFFFF)
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    var _modifier = modifier
        .scale(scale)
        .shadow(
            elevation = if(isDark) 8.dp else 16.dp,
            shape = shape,
            spotColor = Color(0x1A000000),
            ambientColor = Color(0x0D000000)
        )
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    gradientStart,
                    Color.Transparent
                )
            )
        )
        .border(
            width = 1.dp,
            color = borderColor,
            shape = shape
        )
        .background(MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.4f else 0.8f))
        
    if (onClick != null) {
        _modifier = _modifier.clickable(
            interactionSource = interactionSource, 
            indication = null, 
            onClick = onClick
        )
    }

    Box(
        modifier = _modifier.padding(padding),
        contentAlignment = Alignment.TopStart,
        content = content
    )
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) Color(0x40FFFFFF) else Color(0x26000000)
    val bgColor = MaterialTheme.colorScheme.surface.copy(alpha = if(isDark) 0.3f else 0.7f)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(0.5.dp, borderColor, shape),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: @Composable (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f)
    )

    Row(
        modifier = modifier
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = color.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, color = color, style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

