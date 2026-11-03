package com.inci.campuscompanion.data.remote

import com.google.gson.annotations.SerializedName

data class EventDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("location") val location: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)
