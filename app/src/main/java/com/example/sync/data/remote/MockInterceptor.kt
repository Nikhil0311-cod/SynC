package com.example.sync.data.remote

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import com.example.sync.data.model.Student
import com.google.gson.Gson

class MockInterceptor : Interceptor {
    private val gson = Gson()
    
    // In-memory mock "database"
    private var mockStudent = Student(
        id = "2026-ECE-1001",
        name = "Nikhil Sharan",
        department = "ECE",
        year = "1st Year",
        email = "learnnikhil123@gmail.com",
        photoUrl = "https://drive.google.com/drive/folders/1cM6SQKMsbttAtFKe_KUEzXZ5wa5JtWJc"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val uri = request.url.toUri().toString()
        val method = request.method

        var responseString: String

        when {
            // GET Profile
            uri.endsWith("student/profile") && method == "GET" -> {
                responseString = gson.toJson(mockStudent)
            }
            
            // PUT Profile (Update)
            uri.endsWith("student/profile") && method == "PUT" -> {
                // In a real app, we'd parse the request body. For mocking, we'll just return success.
                // Normally we'd update `mockStudent` here.
                responseString = gson.toJson(mockStudent)
            }

            // POST Profile Picture
            uri.endsWith("student/profile/picture") && method == "POST" -> {
                // Fake a new URL for the uploaded photo
                mockStudent = mockStudent.copy(photoUrl = "https://randomuser.me/api/portraits/lego/1.jpg")
                responseString = gson.toJson(mockStudent)
            }
            
            uri.endsWith("polls") -> responseString = """
                [
                    {
                        "id": "p1",
                        "question": "Which tech stack should we use for the Hackathon?",
                        "totalVotes": 128,
                        "hasVoted": false,
                        "options": [
                            {"id": "o1", "text": "Kotlin & Ktor", "voteCount": 80, "percentage": 62.5},
                            {"id": "o2", "text": "Node.js & Express", "voteCount": 48, "percentage": 37.5}
                        ]
                    }
                ]
            """.trimIndent()
            
            uri.endsWith("lost-found") -> responseString = """
                [
                    {
                        "id": "i1",
                        "title": "MacBook Air M2",
                        "description": "Found near the library cafeteria. Has a blue skin.",
                        "location": "Library Cafeteria",
                        "datePosted": "Aug 24",
                        "isRecovered": false,
                        "postedBy": "Admin"
                    }
                ]
            """.trimIndent()
            
            uri.endsWith("announcements") -> responseString = """
                [
                    {
                        "id": "a1",
                        "title": "Campus Hackathon 2026",
                        "content": "Registration is now open! Sign up by Friday.",
                        "date": "Aug 24"
                    }
                ]
            """.trimIndent()
            
            else -> responseString = "{}"
        }

        return Response.Builder()
            .code(200)
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("content-type", "application/json")
            .build()
    }
}
