package com.inci.campuscompanion.data

import com.inci.campuscompanion.data.remote.CampusApi
import com.inci.campuscompanion.data.remote.EventDto
import com.inci.campuscompanion.model.CampusEvent
import com.inci.campuscompanion.model.toCampusEvent
import kotlinx.coroutines.delay

class EventsRepository(
    private val campusApi: CampusApi
) {
    var simulateError: Boolean = false

    suspend fun getEvents(): Result<List<CampusEvent>> {
        if (simulateError) {
            delay(800)
            return Result.failure(Exception("Simulated network error: Unable to connect to Campus API server."))
        }

        return runCatching {
            val dtos = campusApi.getEvents()
            dtos.map { it.toCampusEvent() }
        }.recoverCatching {
            // Fallback to sample campus events if remote endpoint is unreachable
            delay(600)
            getSampleEvents()
        }
    }

    private fun getSampleEvents(): List<CampusEvent> {
        return listOf(
            EventDto(
                id = 1,
                title = "Campus Tech Symposium 2025",
                description = "Annual technology showcase presenting student projects, AI demos, and guest speakers from leading tech companies.",
                location = "Student Union Hall A",
                date = "Oct 15, 10:00 AM",
                category = "Technology"
            ),
            EventDto(
                id = 2,
                title = "Fall Career & Internship Fair",
                description = "Meet representatives from over 50 top engineering, finance, and technology companies looking for interns and graduates.",
                location = "Campus Recreation Center",
                date = "Oct 18, 01:00 PM",
                category = "Career"
            ),
            EventDto(
                id = 3,
                title = "Hackathon: Build for Campus",
                description = "24-hour student hackathon focused on building software solutions for campus community challenges. Food provided!",
                location = "Computer Science Building",
                date = "Oct 22, 09:00 AM",
                category = "Coding"
            ),
            EventDto(
                id = 4,
                title = "Campus Music & Food Festival",
                description = "Live performances by student bands, local artists, and food trucks across the main quad.",
                location = "Main Quad",
                date = "Oct 25, 05:00 PM",
                category = "Social"
            ),
            EventDto(
                id = 5,
                title = "AI Ethics & Future of Work Keynote",
                description = "Distinguished guest lecture discussing ethics in artificial intelligence and automation.",
                location = "Auditorium 101",
                date = "Nov 02, 03:00 PM",
                category = "Academic"
            )
        ).map { it.toCampusEvent() }
    }
}
