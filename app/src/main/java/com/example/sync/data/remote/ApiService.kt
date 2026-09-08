package com.example.sync.data.remote

import com.example.sync.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface defining the network endpoints for the SynC application.
 * All calls are asynchronous suspend functions.
 */
interface ApiService {
    /** Auth Endpoints */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseDto

    /** Profile Management */
    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(@Part image: MultipartBody.Part): UserDto

    /** Dashboard Content (Announcements) */
    @GET("announcements")
    suspend fun getAnnouncements(): List<AnnouncementDto>

    @POST("announcements")
    suspend fun createAnnouncement(@Body request: CreateAnnouncementRequest): AnnouncementDto

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: Int): Response<Unit>

    /** Campus Polls */
    @GET("polls")
    suspend fun getPolls(): List<PollDto>

    @POST("polls")
    suspend fun createPoll(@Body request: CreatePollRequest): PollDto

    @POST("polls/{id}/vote")
    suspend fun votePoll(
        @Path("id") id: Int,
        @Body request: VoteRequest
    ): PollDto

    @DELETE("polls/{id}")
    suspend fun deletePoll(@Path("id") id: Int): Response<Unit>

    /** Lost and Found Services */
    @GET("lost-found")
    suspend fun getLostFound(): List<LostFoundDto>

    @POST("lost-found")
    suspend fun createLostFound(@Body request: CreateLostFoundRequest): LostFoundDto

    @DELETE("lost-found/{id}")
    suspend fun deleteLostFound(@Path("id") id: Int): Response<Unit>

    /** Learning Materials and Document Sharing */
    @GET("materials")
    suspend fun getMaterials(): List<MaterialDto>

    @Multipart
    @POST("materials")
    suspend fun uploadMaterial(
        @Part("name") name: RequestBody,
        @Part("course") course: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("user_id") userId: RequestBody,
        @Part file: MultipartBody.Part
    ): MaterialDto

    @DELETE("materials/{id}")
    suspend fun deleteMaterial(@Path("id") id: Int): Response<Unit>
}
