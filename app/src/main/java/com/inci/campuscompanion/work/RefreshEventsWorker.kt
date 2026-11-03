package com.inci.campuscompanion.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.inci.campuscompanion.data.AppDatabase
import com.inci.campuscompanion.data.EventsRepository
import com.inci.campuscompanion.data.remote.CampusApi

// Runs outside any screen, so it builds its own repository instead of borrowing the ViewModel's.
class RefreshEventsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventsRepository(CampusApi.create(), database.eventDao())
        repository.simulateError = inputData.getBoolean(KEY_SIMULATE_ERROR, false)

        return repository.refreshEvents().fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                // runAttemptCount starts at 0, so this allows MAX_ATTEMPTS runs in total.
                if (runAttemptCount + 1 < MAX_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Refresh failed")))
                }
            }
        )
    }

    companion object {
        const val KEY_SIMULATE_ERROR = "simulate_error"
        const val KEY_ERROR = "error"
        const val MAX_ATTEMPTS = 3
    }
}
