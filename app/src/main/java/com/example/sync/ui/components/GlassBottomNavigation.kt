package com.example.sync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sync.ui.theme.SynCTheme

@Composable
fun GlassBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = SynCTheme.glassColors

    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(glassColors.glassWhite.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = Icons.Default.Home,
                selected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            NavigationItem(
                icon = Icons.Default.Poll,
                selected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            NavigationItem(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                selected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            NavigationItem(
                icon = Icons.Default.Search,
                selected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
            NavigationItem(
                icon = Icons.Default.Person,
                selected = selectedIndex == 4,
                onClick = { onItemSelected(4) }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (selected) glassColors.textPrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) glassColors.textPrimary else glassColors.textSecondary,
            modifier = Modifier.size(28.dp)
        )
    }
}
