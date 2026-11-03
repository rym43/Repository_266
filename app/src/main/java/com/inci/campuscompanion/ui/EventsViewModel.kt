package com.inci.campuscompanion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.inci.campuscompanion.data.EventsRepository
import com.inci.campuscompanion.model.CampusEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Success(val events: List<CampusEvent>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class EventsViewModel(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _isSimulatingError = MutableStateFlow(false)
    val isSimulatingError: StateFlow<Boolean> = _isSimulatingError.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            eventsRepository.getEvents()
                .onSuccess { events ->
                    _uiState.value = EventsUiState.Success(events)
                }
                .onFailure { exception ->
                    _uiState.value = EventsUiState.Error(
                        exception.localizedMessage ?: "Failed to load events. Please check your network connection."
                    )
                }
        }
    }

    fun toggleSimulateError(enabled: Boolean) {
        _isSimulatingError.value = enabled
        eventsRepository.simulateError = enabled
        loadEvents()
    }

    companion object {
        fun Factory(eventsRepository: EventsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventsViewModel(eventsRepository) as T
                }
            }
    }
}
