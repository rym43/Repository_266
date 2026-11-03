package com.inci.campuscompanion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inci.campuscompanion.data.EventsRepository
import com.inci.campuscompanion.model.CampusEvent
import com.inci.campuscompanion.work.EventsRefreshScheduler
import com.inci.campuscompanion.work.RefreshStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Success(val events: List<CampusEvent>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class EventsViewModel(
    eventsRepository: EventsRepository,
    private val refreshScheduler: EventsRefreshScheduler
) : ViewModel() {

    val refreshStatus: StateFlow<RefreshStatus> = refreshScheduler.status
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RefreshStatus.Idle)

    // Cached events win: they stay on screen while refresh work waits, runs or fails.
    // Loading/Error only appear when the cache is still empty.
    val uiState: StateFlow<EventsUiState> =
        combine(eventsRepository.cachedEvents, refreshStatus) { events, status ->
            when {
                events.isNotEmpty() -> EventsUiState.Success(events)
                status is RefreshStatus.Failed -> EventsUiState.Error(status.message)
                status is RefreshStatus.Succeeded -> EventsUiState.Success(emptyList())
                else -> EventsUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState.Loading)

    private val _isSimulatingError = MutableStateFlow(false)
    val isSimulatingError: StateFlow<Boolean> = _isSimulatingError.asStateFlow()

    init {
        refreshEvents()
    }

    fun refreshEvents() {
        refreshScheduler.requestRefresh(simulateError = _isSimulatingError.value)
    }

    fun toggleSimulateError(enabled: Boolean) {
        _isSimulatingError.value = enabled
        refreshEvents()
    }

    companion object {
        fun Factory(
            eventsRepository: EventsRepository,
            refreshScheduler: EventsRefreshScheduler
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventsViewModel(eventsRepository, refreshScheduler) as T
                }
            }
    }
}
