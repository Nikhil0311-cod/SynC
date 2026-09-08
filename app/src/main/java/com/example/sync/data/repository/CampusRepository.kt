package com.example.sync.data.repository

import com.example.sync.data.model.*
import com.example.sync.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import retrofit2.Response

/**
 * The primary data source for the application, abstracting network calls via [ApiService].
 * Uses Kotlin Flows to return asynchronous results wrapped in [Result].
 */
class CampusRepository(private val apiService: ApiService) {

    /**
     * Registers a new user account.
     */
    fun register(request: RegisterRequest): Flow<Result<AuthResponseDto>> = flow {
        try {
            emit(Result.success(apiService.register(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     */
    fun login(request: LoginRequest): Flow<Result<AuthResponseDto>> = flow {
        try {
            emit(Result.success(apiService.login(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Fetches the profile details of the currently authenticated user.
     */
    fun getCurrentUser(): Flow<Result<UserDto>> = flow {
        try {
            emit(Result.success(apiService.getCurrentUser()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Updates the user's name or avatar URL (text-based update).
     */
    fun updateProfile(request: UpdateProfileRequest): Flow<Result<UserDto>> = flow {
        try {
            emit(Result.success(apiService.updateProfile(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Uploads a physical image file to update the user's profile picture.
     */
    fun uploadAvatar(imagePart: okhttp3.MultipartBody.Part): Flow<Result<UserDto>> = flow {
        try {
            emit(Result.success(apiService.uploadAvatar(imagePart)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // --- Announcements & Dashboard Content ---

    /**
     * Retrieves all campus announcements.
     */
    fun getAnnouncements(): Flow<Result<List<AnnouncementDto>>> = flow {
        try {
            emit(Result.success(apiService.getAnnouncements()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Posts a new announcement to the campus feed.
     */
    fun createAnnouncement(request: CreateAnnouncementRequest): Flow<Result<AnnouncementDto>> = flow {
        try {
            emit(Result.success(apiService.createAnnouncement(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Removes an announcement by its ID.
     */
    fun deleteAnnouncement(id: Int): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteAnnouncement(id)
            if (response.isSuccessful) emit(Result.success(Unit))
            else emit(Result.failure(Exception("Error deleting announcement")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // --- Interactive Polls ---

    /**
     * Lists all active campus polls.
     */
    fun getPolls(): Flow<Result<List<PollDto>>> = flow {
        try {
            emit(Result.success(apiService.getPolls()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Creates a new poll for students to vote on.
     */
    fun createPoll(request: CreatePollRequest): Flow<Result<PollDto>> = flow {
        try {
            emit(Result.success(apiService.createPoll(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Casts a vote for a specific option in a poll.
     */
    fun votePoll(id: Int, optionIndex: Int): Flow<Result<PollDto>> = flow {
        try {
            emit(Result.success(apiService.votePoll(id, VoteRequest(optionIndex))))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes a poll.
     */
    fun deletePoll(id: Int): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deletePoll(id)
            if (response.isSuccessful) emit(Result.success(Unit))
            else emit(Result.failure(Exception("Error deleting poll")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // --- Lost & Found ---

    /**
     * Retrieves the list of lost or found items.
     */
    fun getLostFound(): Flow<Result<List<LostFoundDto>>> = flow {
        try {
            emit(Result.success(apiService.getLostFound()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Reports a new lost or found item.
     */
    fun createLostFound(request: CreateLostFoundRequest): Flow<Result<LostFoundDto>> = flow {
        try {
            emit(Result.success(apiService.createLostFound(request)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Removes a lost and found entry.
     */
    fun deleteLostFound(id: Int): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteLostFound(id)
            if (response.isSuccessful) emit(Result.success(Unit))
            else emit(Result.failure(Exception("Error deleting lost item")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // --- Learning Materials & File Sharing ---

    /**
     * Lists all uploaded learning materials.
     */
    fun getMaterials(): Flow<Result<List<MaterialDto>>> = flow {
        try {
            emit(Result.success(apiService.getMaterials()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Uploads a learning material file with associated metadata.
     */
    fun uploadMaterial(
        name: RequestBody,
        course: RequestBody,
        description: RequestBody?,
        userId: RequestBody,
        filePart: okhttp3.MultipartBody.Part
    ): Flow<Result<MaterialDto>> = flow {
        try {
            emit(Result.success(apiService.uploadMaterial(name, course, description, userId, filePart)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Deletes a learning material.
     */
    fun deleteMaterial(id: Int): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.deleteMaterial(id)
            if (response.isSuccessful) emit(Result.success(Unit))
            else emit(Result.failure(Exception("Error deleting material")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
