package com.inci.campuscompanion.model

import com.inci.campuscompanion.data.remote.EventDto

data class CampusEvent(
    val id: Int,
    val title: String,
    val description: String,
    val location: String,
    val date: String,
    val category: String = "General",
    val imageUrl: String? = null
)

fun EventDto.toCampusEvent(): CampusEvent {
    return CampusEvent(
        id = id ?: 0,
        title = title ?: "Untitled Event",
        description = description ?: "No description available.",
        location = location ?: "Campus Center",
        date = date ?: "TBD",
        category = category ?: "General",
        imageUrl = imageUrl
    )
}
