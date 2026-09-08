package com.example.sync.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sync.data.model.AnnouncementDto
import com.example.sync.data.model.UserDto
import com.example.sync.data.remote.UrlUtils
import com.example.sync.ui.components.GlassCard
import com.example.sync.ui.theme.SynCTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPolls: () -> Unit,
    onNavigateToLostFound: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddModal by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        HomeContent(
            uiState = uiState,
            onNavigateToPolls = onNavigateToPolls,
            onNavigateToLostFound = onNavigateToLostFound,
            onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) },
            onAddClick = { showAddModal = true }
        )
    }

    if (showAddModal) {
        AddAnnouncementDialog(
            onDismiss = { showAddModal = false },
            onConfirm = { title, category, body ->
                viewModel.createAnnouncement(title, category, body)
                showAddModal = false
            }
        )
    }
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onNavigateToPolls: () -> Unit,
    onNavigateToLostFound: () -> Unit,
    onDeleteAnnouncement: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors

    Scaffold(
        floatingActionButton = {
            if (uiState.isAdmin) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = glassColors.textPrimary,
                    contentColor = if (MaterialTheme.colorScheme.isLight()) Color.White else Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Announcement")
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glassColors.backgroundGradient)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderSection()
                }

                uiState.user?.let { user ->
                    item {
                        val avatarUrl = UrlUtils.sanitizeUrl(user.avatarUrl)
                        ProfileBanner(user.copy(avatarUrl = avatarUrl))
                    }
                }

                item {
                    val pollPercentage = uiState.activePoll?.options?.firstOrNull()?.votes?.let { v ->
                        val total = uiState.activePoll.options.sumOf { it.votes }
                        if (total > 0) (v * 100 / total) else 0
                    } ?: 0

                    FeatureGrid(
                        pollPercentage = pollPercentage,
                        lostItemsCount = uiState.lostItemsCount,
                        onPollClick = onNavigateToPolls,
                        onLostFoundClick = onNavigateToLostFound
                    )
                }

                item {
                    Text(
                        text = "Campus Feed",
                        style = MaterialTheme.typography.titleLarge,
                        color = glassColors.textPrimary
                    )
                }

                items(uiState.announcements) { announcement ->
                    AnnouncementCard(
                        announcement = announcement,
                        showDelete = uiState.isAdmin,
                        onDelete = { announcement.id?.let { onDeleteAnnouncement(it) } }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(
    announcement: AnnouncementDto,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = announcement.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = glassColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(
                        containerColor = glassColors.textPrimary.copy(alpha = 0.1f),
                        contentColor = glassColors.textPrimary
                    ) {
                        Text(announcement.category, fontSize = 10.sp)
                    }
                    if (showDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Text(
                text = announcement.createdAt ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = glassColors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = announcement.body,
                style = MaterialTheme.typography.bodyMedium,
                color = glassColors.textSecondary,
                maxLines = 3
            )
        }
    }
}

@Composable
fun AddAnnouncementDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Events") }
    var body by remember { mutableStateOf("") }
    
    val glassColors = SynCTheme.glassColors

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = glassColors.glassWhite.copy(alpha = 0.85f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "New Announcement", style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Events, Notice)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Content") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = glassColors.textSecondary) }
                    Button(
                        onClick = { onConfirm(title, category, body) },
                        enabled = title.isNotBlank() && body.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5f

@Composable
fun HeaderSection() {
    val glassColors = SynCTheme.glassColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SynC",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = glassColors.textPrimary
        )
    }
}

@Composable
fun ProfileBanner(user: UserDto) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl != null) {
                    val imageUrl = if (user.avatarUrl.contains("?")) {
                        "${user.avatarUrl}&t=${System.currentTimeMillis()}"
                    } else {
                        "${user.avatarUrl}?t=${System.currentTimeMillis()}"
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = glassColors.textSecondary)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = user.name, style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                Text(text = "${user.role} | ID: ${user.id}", style = MaterialTheme.typography.bodyMedium, color = glassColors.textSecondary)
            }
        }
    }
}

@Composable
fun FeatureGrid(
    pollPercentage: Int,
    lostItemsCount: Int,
    onPollClick: () -> Unit,
    onLostFoundClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Active Polls",
                subtitle = if (pollPercentage > 0) "$pollPercentage% Voted" else "New Poll Active",
                icon = Icons.Default.Star,
                buttonText = "Vote Now",
                onClick = onPollClick
            )
            FeatureCard(
                modifier = Modifier.weight(1f),
                title = "Lost & Found",
                subtitle = if (lostItemsCount > 0) "$lostItemsCount New Items" else "No new items",
                icon = Icons.Default.Search,
                buttonText = "View Items",
                onClick = onLostFoundClick
            )
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = null, tint = glassColors.textPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = glassColors.textPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = glassColors.textSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = glassColors.textPrimary.copy(alpha = 0.1f))
            ) {
                Text(text = buttonText, color = glassColors.textPrimary, fontSize = 10.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderSectionPreview() {
    SynCTheme {
        HeaderSection()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileBannerPreview() {
    SynCTheme {
        ProfileBanner(
            UserDto(123, "John Doe", "john@example.com", null, "student", "2026-09-06")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureGridPreview() {
    SynCTheme {
        FeatureGrid(pollPercentage = 68, lostItemsCount = 3, onPollClick = {}, onLostFoundClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun FeatureCardPreview() {
    SynCTheme {
        FeatureCard(
            title = "Active Polls",
            subtitle = "68% Voted",
            icon = Icons.Default.Star,
            buttonText = "Vote Now",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    SynCTheme {
        HomeContent(
            uiState = HomeUiState(
                user = UserDto(123, "John Doe", "john@example.com", null, "student", "2026-09-06"),
                announcements = listOf(
                    AnnouncementDto(1, "Hackathon 2026", "Events", "Join the biggest event!", "Aug 24"),
                    AnnouncementDto(2, "Library Notice", "General", "New books available.", "Aug 23")
                ),
                isAdmin = true
            ),
            onNavigateToPolls = {},
            onNavigateToLostFound = {},
            onDeleteAnnouncement = {},
            onAddClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenDarkPreview() {
    SynCTheme(darkTheme = true) {
        HomeContent(
            uiState = HomeUiState(
                user = UserDto(123, "John Doe", "john@example.com", null, "student", "2026-09-06"),
                announcements = listOf(
                    AnnouncementDto(1, "Hackathon 2026", "Events", "Join the biggest event!", "Aug 24")
                ),
                isAdmin = false
            ),
            onNavigateToPolls = {},
            onNavigateToLostFound = {},
            onDeleteAnnouncement = {},
            onAddClick = {}
        )
    }
}
