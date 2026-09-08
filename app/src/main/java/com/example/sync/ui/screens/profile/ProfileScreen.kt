package com.example.sync.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sync.data.model.UserDto
import com.example.sync.data.remote.UrlUtils
import com.example.sync.ui.components.GlassCard
import com.example.sync.ui.theme.SynCTheme
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditModal by remember { mutableStateOf(value = false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            val file = context.getFileFromUri(it)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
            viewModel.uploadAvatar(body)
        }
    }

    ProfileContent(
        uiState = uiState,
        onLogout = { viewModel.logout() },
        onEditClick = { if (uiState.user != null) showEditModal = true },
        onImageClick = { imagePickerLauncher.launch("image/*") }
    )

    val currentUser = uiState.user
    if (showEditModal && currentUser != null) {
        EditProfileDialog(
            user = currentUser,
            isLoading = uiState.isLoading,
            onDismiss = { showEditModal = false },
            onConfirm = { name, avatarUrl ->
                viewModel.updateProfile(name, avatarUrl)
            }
        )
        
        LaunchedEffect(uiState.isLoading) {
            if (!uiState.isLoading && uiState.error == null) {
                showEditModal = false
            }
        }
    }
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onLogout: () -> Unit,
    onEditClick: () -> Unit,
    onImageClick: () -> Unit
) {
    val glassColors = SynCTheme.glassColors
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glassColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))
                Text(
                    text = "Student Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = glassColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = glassColors.textPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                val user = uiState.user
                val avatarUrl = UrlUtils.sanitizeUrl(user?.avatarUrl)
                if (avatarUrl != null) {
                    val imageUrl = if (avatarUrl.contains("?")) {
                        "${avatarUrl}&t=${System.currentTimeMillis()}"
                    } else {
                        "${avatarUrl}?t=${System.currentTimeMillis()}"
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = glassColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            uiState.user?.let { user ->
                Text(text = user.name, style = MaterialTheme.typography.headlineSmall, color = glassColors.textPrimary)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium, color = glassColors.textSecondary)
                
                Spacer(modifier = Modifier.height(32.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileInfoRow(label = "Role", value = user.role)
                        ProfileInfoRow(label = "Member Since", value = user.createdAt ?: "N/A")
                        ProfileInfoRow(label = "User ID", value = user.id.toString())
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(modifier = Modifier.weight(1f), label = "Active Polls", value = uiState.pollsCount.toString())
                    StatCard(modifier = Modifier.weight(1f), label = "Items Posted", value = uiState.itemsPosted.toString())
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun EditProfileDialog(
    user: UserDto,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    val glassColors = SynCTheme.glassColors
    var name by remember { mutableStateOf(user.name) }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl ?: "") }

    Dialog(onDismissRequest = if (isLoading) ({}) else onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = glassColors.glassWhite.copy(alpha = 0.85f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Edit Profile", style = MaterialTheme.typography.titleLarge, color = glassColors.textPrimary)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
                OutlinedTextField(value = avatarUrl, onValueChange = { avatarUrl = it }, label = { Text("Avatar URL") }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel", color = glassColors.textSecondary) }
                    Button(
                        onClick = { onConfirm(name, avatarUrl.takeIf { it.isNotBlank() }) },
                        enabled = !isLoading && name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    val glassColors = SynCTheme.glassColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = glassColors.textSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = glassColors.textPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    val glassColors = SynCTheme.glassColors
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, color = glassColors.textPrimary, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = glassColors.textSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileInfoRowPreview() {
    SynCTheme {
        ProfileInfoRow(label = "Department", value = "Computer Science")
    }
}

@Preview(showBackground = true)
@Composable
fun StatCardPreview() {
    SynCTheme {
        StatCard(label = "Polls Joined", value = "12")
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    SynCTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = UserDto(101, "Jane Doe", "jane@example.com", null, "student", "2026-09-06"),
                pollsCount = 5,
                itemsPosted = 2
            ),
            onLogout = {},
            onEditClick = {},
            onImageClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenDarkPreview() {
    SynCTheme(darkTheme = true) {
        ProfileContent(
            uiState = ProfileUiState(
                user = UserDto(101, "Jane Doe", "jane@example.com", null, "student", "2026-09-06"),
                pollsCount = 5,
                itemsPosted = 2
            ),
            onLogout = {},
            onEditClick = {},
            onImageClick = {}
        )
    }
}

// Extension to get File from Uri
fun android.content.Context.getFileFromUri(uri: Uri): File {
    val inputStream = contentResolver.openInputStream(uri)
    val extension = contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
    val file = File(cacheDir, "temp_file_${System.currentTimeMillis()}.$extension")
    val outputStream = FileOutputStream(file)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return file
}
