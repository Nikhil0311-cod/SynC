package com.example.sync.data.remote

/**
 * Utility class for handling URL transformations and sanitization.
 */
object UrlUtils {
    /**
     * Fixes URLs coming from the backend.
     * Replaces local hostnames with the actual Base URL being used by the app to ensure
     * media (like avatars) load correctly on physical devices or emulators.
     *
     * @param url The raw URL string from the backend.
     * @param currentBase The current base URL of the API server (e.g., "http://10.0.2.2:8080").
     * @return The sanitized absolute URL.
     */
    fun sanitizeUrl(url: String?, currentBase: String = "http://<YOUR_API_BASE_URL>"): String? {
        if (url == null) return null
        
        return url.replace("http://localhost:8080", currentBase)
                  .replace("http://127.0.0.1:8080", currentBase)
                  .replace("localhost", currentBase.substringAfter("://").substringBefore("/"))
    }
}
