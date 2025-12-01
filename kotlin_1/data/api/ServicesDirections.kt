package com.example.kotlin_1.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ServiceDirections {

    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "walking",
        @Query("key") apiKey: String
    ): Response<DirectionsResponseDTO>
}