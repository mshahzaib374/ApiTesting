package com.example.apitesting.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class VideoRequest(
    val videoUrl: String
)

interface ApiService {
    @POST("download-video-content")
    fun getVideo(@Body request: VideoRequest): Call<ResponseBody>
}