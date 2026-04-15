package com.dodamsoft.ajangajang.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.usecase.GetRecentCheckRecordsUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import com.dodamsoft.ajangajang.domain.usecase.SetPrimaryChildUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val activeChildId: Long? = null,
    val recentRecords: List<CheckRecord> = emptyList(),
    val loading: Boolean = true,
) {
    val isGuest: Boolean get() = profiles.isEmpty()
    val activeChild: ChildProfile?
        get() = profiles.firstOrNull { it.id == activeChildId }
            ?: profiles.firstOrNull { it.isPrimary }
            ?: profiles.firstOrNull()
}

class HomeViewModel(
    observeChildProfiles: ObserveChildProfilesUseCase,
    private val getRecentRecords: GetRecentCheckRecordsUseCase,
    private val appPreferences: AppPreferencesRepository,
    private val setPrimaryChild: SetPrimaryChildUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeChildProfiles()
                .combine(appPreferences.preferences) { profiles, prefs -> profiles to prefs }
                .collect { (profiles, prefs) ->
                    _state.update {
                        it.copy(
                            profiles = profiles,
                            activeChildId = prefs.activeChildId,
                            loading = false,
                        )
                    }
                    refreshRecentRecords()
                }
        }
    }

    private fun refreshRecentRecords() {
        viewModelScope.launch {
            val recent = runCatching { getRecentRecords(3) }.getOrDefault(emptyList())
            _state.update { it.copy(recentRecords = recent) }
        }
    }

    fun selectChild(id: Long) {
        viewModelScope.launch {
            appPreferences.setActiveChildId(id)
            setPrimaryChild(id)
        }
    }
}
