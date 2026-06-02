package com.example.module6.data.remote

import com.example.module6.data.model.PhotoDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface PicsumApiService {
    @GET("v2/list")
    suspend fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 24
    ): List<PhotoDto>

    @Streaming
    @GET
    suspend fun downloadPhoto(@Url imageUrl: String): ResponseBody
}
