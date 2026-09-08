package com.example.sync.ui.screens.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sync.data.local.AuthManager
import com.example.sync.data.model.MaterialDto
import com.example.sync.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

data class MaterialsUiState(
    val isLoading: Boolean = false,
    val materials: List<MaterialDto> = emptyList(),
    val searchQuery: String = "",
    val isAdmin: Boolean = false,
    val error: String? = null
)

class MaterialsViewModel(
    private val repository: CampusRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialsUiState())
    val uiState: StateFlow<MaterialsUiState> = _uiState.asStateFlow()

    init {
        loadMaterials()
        observeUserRole()
    }

    private fun observeUserRole() {
        viewModelScope.launch {
            authManager.userRole.collect { role ->
                _uiState.update { it.copy(isAdmin = role == "admin") }
            }
        }
    }

    fun refresh() {
        loadMaterials()
    }

    fun loadMaterials() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getMaterials().collect { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        materials = result.getOrNull() ?: emptyList(),
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteMaterial(id).collect { result ->
                if (result.isSuccess) {
                    loadMaterials()
                }
            }
        }
    }

    fun uploadMaterial(name: String, course: String, description: String?, filePart: MultipartBody.Part) {
        viewModelScope.launch {
            val userId = authManager.userId.first()
            if (userId == -1) return@launch

            val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val courseBody = course.toRequestBody("text/plain".toMediaTypeOrNull())
            val descBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            _uiState.update { it.copy(isLoading = true) }
            repository.uploadMaterial(nameBody, courseBody, descBody, userIdBody, filePart).collect { result ->
                if (result.isSuccess) {
                    loadMaterials()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message) }
                }
            }
        }
    }
}
