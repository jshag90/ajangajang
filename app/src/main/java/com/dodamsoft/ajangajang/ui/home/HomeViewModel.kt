package com.dodamsoft.ajangajang.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.resolveActive
import com.dodamsoft.ajangajang.domain.usecase.ObserveAllCheckRecordsUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import com.dodamsoft.ajangajang.domain.usecase.SetPrimaryChildUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val RECENT_RECORDS_LIMIT = 3

data class HomeUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val activeChildId: Long? = null,
    val recentRecords: List<CheckRecord> = emptyList(),
    val loading: Boolean = true,
) {
    val isGuest: Boolean get() = profiles.isEmpty()
    val activeChild: ChildProfile? get() = profiles.resolveActive(activeChildId)
}

class HomeViewModel(
    observeChildProfiles: ObserveChildProfilesUseCase,
    observeAllRecords: ObserveAllCheckRecordsUseCase,
    private val appPreferences: AppPreferencesRepository,
    private val setPrimaryChild: SetPrimaryChildUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeChildProfiles(),
                appPreferences.preferences,
                observeAllRecords(),
            ) { profiles, prefs, records ->
                HomeUiState(
                    profiles = profiles,
                    activeChildId = prefs.activeChildId,
                    recentRecords = records.take(RECENT_RECORDS_LIMIT),
                    loading = false,
                )
            }.collect { next -> _state.value = next }
        }
    }

    fun selectChild(id: Long) {
        viewModelScope.launch {
            appPreferences.setActiveChildId(id)
            setPrimaryChild(id)
        }
    }
}
