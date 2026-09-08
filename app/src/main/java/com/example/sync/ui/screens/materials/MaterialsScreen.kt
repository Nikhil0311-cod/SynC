package com.example.sync.ui.screens.materials

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sync.data.model.MaterialDto
import com.example.sync.data.remote.UrlUtils
import com.example.sync.ui.components.GlassCard
import com.example.sync.ui.screens.profile.getFileFromUri
import com.example.sync.ui.theme.SynCTheme
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

@Composable
fun MaterialsScreen(viewModel: MaterialsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddModal by remember { mutableStateOf(false) }
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        MaterialsContent(
            uiState = uiState,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onDelete = { viewModel.deleteMaterial(it) },
            onDownload = { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MaterialsScreen", "Error opening URL: $url", e)
                }
            },
            onAddClick = { showAddModal = true }
        )
    }

    if (showAddModal) {
        AddMaterialDialog(
            onDismiss = { showAddModal = false },
            onConfirm = { name, course, desc, uri ->
                val file = context.getFileFromUri(uri)
                val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                viewModel.uploadMaterial(name, course, desc, body)
                showAddModal = false
            }
        )
    }
}

@Composable
fun MaterialsContent(
    uiState: MaterialsUiState,
    onSearchQueryChange: (String) -> Unit,
    onDelete: (Int) -> Unit,
    onDownload: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    val filteredItems = uiState.materials.filter {
        it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.course.contains(uiState.searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = glassColors.textPrimary,
                contentColor = if (MaterialTheme.colorScheme.isLight()) Color.White else Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Material")
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
                        text = "Learning Materials",
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
                        placeholder = { Text("Search materials or courses...", color = glassColors.textSecondary) },
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

                items(filteredItems) { material ->
                    MaterialCard(
                        material = material,
                        showDelete = uiState.isAdmin,
                        onDownload = { onDownload(UrlUtils.sanitizeUrl(material.fileUrl) ?: material.fileUrl) },
                        onDelete = { material.id?.let { onDelete(it) } }
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
fun MaterialCard(material: MaterialDto, showDelete: Boolean, onDownload: () -> Unit, onDelete: () -> Unit) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = material.name, style = MaterialTheme.typography.titleMedium, color = glassColors.textPrimary)
                    Text(text = material.course, style = MaterialTheme.typography.labelMedium, color = glassColors.textSecondary)
                }
                Row {
                    IconButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = glassColors.textPrimary)
                    }
                    if (showDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            if (!material.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = material.description, style = MaterialTheme.typography.bodyMedium, color = glassColors.textSecondary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Uploaded: ${material.createdAt ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = glassColors.textSecondary)
        }
    }
}

@Composable
fun AddMaterialDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Uri) -> Unit) {
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    val glassColors = SynCTheme.glassColors
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = glassColors.glassWhite.copy(alpha = 0.85f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Add Learning Material", style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Material Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (Optional)") }, modifier = Modifier.fillMaxWidth())
                
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = glassColors.textPrimary.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = glassColors.textPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (selectedFileUri != null) "File Selected" else "Attach File", color = glassColors.textPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = glassColors.textSecondary) }
                    Button(
                        onClick = { selectedFileUri?.let { onConfirm(name, course, desc, it) } },
                        enabled = name.isNotBlank() && course.isNotBlank() && selectedFileUri != null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5f

@Preview(showBackground = true)
@Composable
fun MaterialCardPreview() {
    SynCTheme {
        MaterialCard(
            material = MaterialDto(1, "Notes on Thermodynamics", "Physics 101", "Comprehensive notes on laws of thermo", "url", 1, "2026-09-06"),
            showDelete = true,
            onDownload = {},
            onDelete = {}
        )
    }
}
