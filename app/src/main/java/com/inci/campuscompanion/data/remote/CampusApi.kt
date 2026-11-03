package com.inci.campuscompanion.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CampusApi {
    @GET("events")
    suspend fun getEvents(): List<EventDto>

    companion object {
        const val BASE_URL = "https://api.campuscompanion.example.com/"

        fun create(baseUrl: String = BASE_URL): CampusApi {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CampusApi::class.java)
        }
    }
}
