package com.example.sync.ui.screens.lostfound

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sync.data.model.LostFoundDto
import com.example.sync.ui.components.GlassCard
import com.example.sync.ui.theme.SynCTheme

@Composable
fun LostFoundScreen(viewModel: LostFoundViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddModal by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        LostFoundContent(
            uiState = uiState,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onDelete = { viewModel.deleteItem(it) },
            onAddClick = { showAddModal = true }
        )
    }

    if (showAddModal) {
        AddItemDialog(
            onDismiss = { showAddModal = false },
            onConfirm = { name, status, loc, desc ->
                viewModel.postItem(name, status, loc, desc)
                showAddModal = false
            }
        )
    }
}

@Composable
fun LostFoundContent(
    uiState: LostFoundUiState,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    val filteredItems = uiState.items.filter {
        it.itemName.contains(uiState.searchQuery, ignoreCase = true) ||
                (it.description?.contains(uiState.searchQuery, ignoreCase = true) ?: false)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = glassColors.textPrimary,
                contentColor = if (MaterialTheme.colorScheme.isLight()) Color.White else Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post Item")
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
                    Text(
                        text = "Lost & Found",
                        style = MaterialTheme.typography.headlineMedium,
                        color = glassColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search items...", color = glassColors.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = glassColors.textSecondary) },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = glassColors.textPrimary,
                            unfocusedTextColor = glassColors.textPrimary,
                            focusedBorderColor = glassColors.textPrimary.copy(alpha = 0.5f),
                            unfocusedBorderColor = glassColors.textPrimary.copy(alpha = 0.2f)
                        )
                    )
                }

                items(filteredItems) { item ->
                    LostItemCard(
                        item = item,
                        showDelete = uiState.isAdmin,
                        onDelete = { item.id?.let { onDelete(it) } }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("lost") } // Default to lost
    
    val glassColors = SynCTheme.glassColors

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = glassColors.glassWhite.copy(alpha = 0.85f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Post New Item", style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "lost", onClick = { status = "lost" })
                    Text("Lost", color = glassColors.textPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = status == "found", onClick = { status = "found" })
                    Text("Found", color = glassColors.textPrimary)
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = glassColors.textSecondary) }
                    Button(
                        onClick = { onConfirm(name, status, loc, desc) },
                        enabled = name.isNotBlank(),
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
fun LostItemCard(item: LostFoundDto, showDelete: Boolean, onDelete: () -> Unit) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.itemName, style = MaterialTheme.typography.titleMedium, color = glassColors.textPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = if (item.status == "lost") Color.Red.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f),
                            contentColor = if (item.status == "lost") Color.Red else Color.Green
                        ) {
                            Text(item.status.uppercase(), fontSize = 10.sp)
                        }
                    }
                    Text(text = item.location ?: "Unknown location", style = MaterialTheme.typography.labelMedium, color = glassColors.textSecondary)
                }
                if (showDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description ?: "No description provided", style = MaterialTheme.typography.bodyMedium, color = glassColors.textSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Posted: ${item.createdAt ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = glassColors.textSecondary)
        }
    }
}

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5f

@Preview(showBackground = true)
@Composable
fun LostItemCardPreview() {
    SynCTheme {
        LostItemCard(
            item = LostFoundDto(1, "Wallet", "lost", "Library", "Black leather wallet", "2026-09-06 12:00:00"),
            showDelete = true,
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LostFoundScreenPreview() {
    SynCTheme {
        LostFoundContent(
            uiState = LostFoundUiState(
                items = listOf(
                    LostFoundDto(1, "Keys", "found", "Canteen", "Car keys found in canteen", "2026-09-06 12:00:00")
                ),
                isAdmin = true
            ),
            onSearchQueryChange = {},
            onDelete = {},
            onAddClick = {}
        )
    }
}
