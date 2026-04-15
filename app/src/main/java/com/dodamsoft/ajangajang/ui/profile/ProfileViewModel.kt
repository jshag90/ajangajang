package com.dodamsoft.ajangajang.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.Gender
import com.dodamsoft.ajangajang.domain.usecase.AddChildProfileUseCase
import com.dodamsoft.ajangajang.domain.usecase.DeleteChildProfileUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import com.dodamsoft.ajangajang.domain.usecase.SetPrimaryChildUseCase
import com.dodamsoft.ajangajang.domain.usecase.UpdateChildProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SaveStatus { Idle, Saving, Saved }

data class ProfileUiState(
    val profiles: List<ChildProfile> = emptyList(),
    val loading: Boolean = true,
    val lastError: String? = null,
    val saveStatus: SaveStatus = SaveStatus.Idle,
) {
    val canAddMore: Boolean get() = profiles.size < 3
}

class ProfileViewModel(
    observeChildProfiles: ObserveChildProfilesUseCase,
    private val addChild: AddChildProfileUseCase,
    private val updateChild: UpdateChildProfileUseCase,
    private val deleteChild: DeleteChildProfileUseCase,
    private val setPrimary: SetPrimaryChildUseCase,
    private val appPreferences: AppPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeChildProfiles().collect { list ->
                _state.update { it.copy(profiles = list, loading = false) }
            }
        }
    }

    fun addProfile(
        name: String,
        birthDate: LocalDate,
        gender: Gender,
        photoUri: String?,
    ) {
        if (_state.value.saveStatus == SaveStatus.Saving) return
        _state.update { it.copy(saveStatus = SaveStatus.Saving, lastError = null) }
        viewModelScope.launch {
            runCatching {
                val newProfile = ChildProfile(
                    id = 0L,
                    name = name.trim(),
                    birthDate = birthDate,
                    gender = gender,
                    photoUri = photoUri,
                    isPrimary = false,
                )
                val newId = addChild(newProfile)
                appPreferences.setActiveChildId(newId)
                newId
            }.onSuccess {
                _state.update { it.copy(saveStatus = SaveStatus.Saved, lastError = null) }
            }.onFailure { t ->
                _state.update { it.copy(saveStatus = SaveStatus.Idle, lastError = t.message ?: "저장 실패") }
            }
        }
    }

    fun updateProfile(profile: ChildProfile) {
        viewModelScope.launch { updateChild(profile) }
    }

    fun deleteProfile(profile: ChildProfile) {
        viewModelScope.launch {
            deleteChild(profile)
            if (_state.value.profiles.size <= 1) {
                appPreferences.setActiveChildId(null)
            }
        }
    }

    fun setAsPrimary(id: Long) {
        viewModelScope.launch {
            setPrimary(id)
            appPreferences.setActiveChildId(id)
        }
    }

    fun resetSaveStatus() {
        _state.update { it.copy(saveStatus = SaveStatus.Idle) }
    }
}
