package com.example.sync.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    /**
     * The Base URL for the backend API.
     * For Local Development: Use "http://10.0.2.2:8080/" for Android Emulator.
     * For Production/Physical Device: Use your server's public IP or domain.
     */
    private const val BASE_URL = "http://<YOUR_API_BASE_URL>/"
    private const val USE_MOCK = false

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "application/json")
        
        authToken?.let {
            val token = if (it.startsWith("Bearer ")) it else "Bearer $it"
            requestBuilder.header("Authorization", token)
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .apply {
            addInterceptor(logging)
            addInterceptor(authInterceptor)
            if (USE_MOCK) {
                addInterceptor(MockInterceptor())
            }
        }
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
