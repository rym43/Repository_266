package com.inci.campuscompanion.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.inci.campuscompanion.model.CampusEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id")
    fun getEvents(): Flow<List<CampusEvent>>

    @Upsert
    suspend fun upsertEvents(events: List<CampusEvent>)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    // Swap the whole cache in one transaction so observers never see an empty list mid-refresh.
    @Transaction
    suspend fun replaceEvents(events: List<CampusEvent>) {
        deleteAllEvents()
        upsertEvents(events)
    }
}
