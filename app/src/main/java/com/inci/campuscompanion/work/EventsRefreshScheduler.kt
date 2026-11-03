package com.inci.campuscompanion.work

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

sealed interface RefreshStatus {
    data object Idle : RefreshStatus
    data object WaitingForNetwork : RefreshStatus
    data object Running : RefreshStatus
    data class Retrying(val attempt: Int) : RefreshStatus
    data object Succeeded : RefreshStatus
    data class Failed(val message: String) : RefreshStatus
}

class EventsRefreshScheduler(private val workManager: WorkManager) {

    val status: Flow<RefreshStatus> =
        workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .map { infos -> infos.lastOrNull().toRefreshStatus() }

    fun requestRefresh(simulateError: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<RefreshEventsWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setInputData(workDataOf(RefreshEventsWorker.KEY_SIMULATE_ERROR to simulateError))
            .build()

        // KEEP: while a refresh is still waiting or running, extra taps are ignored
        // instead of queueing duplicates. Finished work is replaced by the new request.
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private fun WorkInfo?.toRefreshStatus(): RefreshStatus = when (this?.state) {
        null, WorkInfo.State.CANCELLED -> RefreshStatus.Idle
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED ->
            // A retry sits in ENQUEUED during its backoff delay.
            if (runAttemptCount > 0) RefreshStatus.Retrying(runAttemptCount) else RefreshStatus.WaitingForNetwork
        WorkInfo.State.RUNNING -> RefreshStatus.Running
        WorkInfo.State.SUCCEEDED -> RefreshStatus.Succeeded
        WorkInfo.State.FAILED -> RefreshStatus.Failed(
            outputData.getString(RefreshEventsWorker.KEY_ERROR) ?: "Refresh failed"
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "refresh_events"
    }
}
