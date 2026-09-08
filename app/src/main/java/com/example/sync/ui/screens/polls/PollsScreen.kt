package com.example.sync.ui.screens.polls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sync.data.model.PollDto
import com.example.sync.data.model.PollOptionDto
import com.example.sync.ui.components.GlassCard
import com.example.sync.ui.theme.SynCTheme

@Composable
fun PollsScreen(viewModel: PollsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddModal by remember { mutableStateOf(false) }
    
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        PollsContent(
            uiState = uiState,
            onVote = { pId, oIdx -> viewModel.vote(pId, oIdx) },
            onDeletePoll = { viewModel.deletePoll(it) },
            onAddClick = { showAddModal = true }
        )
    }

    if (showAddModal) {
        AddPollDialog(
            onDismiss = { showAddModal = false },
            onConfirm = { question, options ->
                viewModel.createPoll(question, options)
                showAddModal = false
            }
        )
    }
}

@Composable
fun PollsContent(
    uiState: PollsUiState,
    onVote: (Int, Int) -> Unit,
    onDeletePoll: (Int) -> Unit,
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
                    Icon(Icons.Default.Add, contentDescription = "Add Poll")
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
                    Text(
                        text = "Active Polls",
                        style = MaterialTheme.typography.headlineMedium,
                        color = glassColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.polls) { poll ->
                    PollCard(
                        poll = poll,
                        showDelete = uiState.isAdmin,
                        canVote = uiState.isAdmin || !poll.hasVoted,
                        onVote = { index -> poll.id?.let { onVote(it, index) } },
                        onDelete = { poll.id?.let { onDeletePoll(it) } }
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
fun PollCard(
    poll: PollDto,
    showDelete: Boolean,
    canVote: Boolean,
    onVote: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    val totalVotes = poll.options.sumOf { it.votes }
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = poll.question,
                        style = MaterialTheme.typography.titleMedium,
                        color = glassColors.textPrimary
                    )
                    if (poll.hasVoted) {
                        Text(
                            text = "You have already voted",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Green.copy(alpha = 0.7f)
                        )
                    }
                }
                if (showDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            poll.options.forEachIndexed { index, option ->
                val percentage = if (totalVotes > 0) (option.votes.toFloat() / totalVotes * 100) else 0f
                PollOptionRow(
                    option = option, 
                    percentage = percentage,
                    enabled = canVote,
                    onClick = { onVote(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$totalVotes total votes",
                style = MaterialTheme.typography.labelMedium,
                color = glassColors.textSecondary
            )
        }
    }
}

@Composable
fun AddPollDialog(onDismiss: () -> Unit, onConfirm: (String, List<String>) -> Unit) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }
    
    val glassColors = SynCTheme.glassColors

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = glassColors.glassWhite.copy(alpha = 0.85f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "New Poll", style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                
                Text(text = "Options", style = MaterialTheme.typography.titleSmall, color = glassColors.textPrimary)
                options.forEachIndexed { index, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = { newValue ->
                            options = options.toMutableList().apply { set(index, newValue) }
                        },
                        label = { Text("Option ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (options.size < 5) {
                    TextButton(onClick = { options = options + "" }) {
                        Text("+ Add Option", color = glassColors.textPrimary)
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = glassColors.textSecondary) }
                    Button(
                        onClick = { onConfirm(question, options.filter { it.isNotBlank() }) },
                        enabled = question.isNotBlank() && options.filter { it.isNotBlank() }.size >= 2,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5f

@Composable
fun PollOptionRow(option: PollOptionDto, percentage: Float, enabled: Boolean, onClick: () -> Unit) {
    val glassColors = SynCTheme.glassColors
    Surface(
        onClick = if (enabled) onClick else ({}),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = if (enabled) 0.1f else 0.05f),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .matchParentSize()
                    .background(glassColors.textPrimary.copy(alpha = 0.15f))
            )
            
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = option.text, style = MaterialTheme.typography.bodyMedium, color = glassColors.textPrimary)
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PollCardPreview() {
    SynCTheme {
        PollCard(
            poll = PollDto(
                1, "Who will win the Hackathon?", listOf(
                    PollOptionDto("Team Alpha", 10),
                    PollOptionDto("Team Beta", 10)
                ), false, "2026-09-06 12:00:00"
            ),
            showDelete = true,
            canVote = true,
            onVote = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PollsScreenPreview() {
    SynCTheme {
        PollsContent(
            uiState = PollsUiState(
                polls = listOf(
                    PollDto(
                        1, "Favorite Language?", listOf(
                            PollOptionDto("Kotlin", 50),
                            PollOptionDto("Java", 30)
                        ), true, "2026-09-06 12:00:00"
                    )
                ),
                isAdmin = true
            ),
            onVote = { _, _ -> },
            onDeletePoll = {},
            onAddClick = {}
        )
    }
}
