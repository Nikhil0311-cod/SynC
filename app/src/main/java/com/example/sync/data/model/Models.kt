package com.example.sync.data.model

import com.google.gson.annotations.SerializedName

// --- Data Models (DTOs) ---

data class AnnouncementDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("body") val body: String,
    @SerializedName("created_at") val createdAt: String?
)

data class CreateAnnouncementRequest(
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("body") val body: String,
    @SerializedName("user_id") val userId: Int
)

data class PollOptionDto(
    @SerializedName("text") val text: String,
    @SerializedName("votes") val votes: Int
)

data class PollDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<PollOptionDto>,
    @SerializedName("has_voted") val hasVoted: Boolean = false,
    @SerializedName("created_at") val createdAt: String?
)

data class CreatePollRequest(
    @SerializedName("question") val question: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("user_id") val userId: Int
)

data class VoteRequest(
    @SerializedName("optionIndex") val optionIndex: Int
)

data class LostFoundDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("status") val status: String,
    @SerializedName("location") val location: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class CreateLostFoundRequest(
    @SerializedName("item_name") val itemName: String,
    @SerializedName("status") val status: String,
    @SerializedName("location") val location: String,
    @SerializedName("description") val description: String,
    @SerializedName("user_id") val userId: Int
)

data class MaterialDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String,
    @SerializedName("course") val course: String,
    @SerializedName("description") val description: String?,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("created_at") val createdAt: String?
)

// --- Auth & User Models ---

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("role") val role: String,
    @SerializedName("created_at") val createdAt: String?
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponseDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class UpdateProfileRequest(
    @SerializedName("name") val name: String,
    @SerializedName("avatar_url") val avatarUrl: String?
)

// --- Domain Models (Keeping existing names to minimize UI breakage where possible) ---
// Note: We will map the DTOs to these or use DTOs directly in the next steps.

data class Student(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("department") val department: String = "",
    @SerializedName("year") val year: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("photoUrl") val photoUrl: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String = "",
    @SerializedName(value = "user", alternate = ["record", "student", "data", "profile"])
    val user: Student? = null
)

data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
