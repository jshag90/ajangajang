package com.dodamsoft.ajangajang.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChecklistStage
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.resolveActive
import com.dodamsoft.ajangajang.domain.usecase.CalculateResultUseCase
import com.dodamsoft.ajangajang.domain.usecase.GetChecklistStageUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import com.dodamsoft.ajangajang.domain.usecase.SaveCheckRecordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChecklistUiState(
    val stage: ChecklistStage? = null,
    val checked: Set<String> = emptySet(),
    val result: CheckResult? = null,
    val resultSaved: Boolean = false,
    val activeChild: ChildProfile? = null,
    val loading: Boolean = true,
    val error: String? = null,
) {
    val totalCount: Int get() = stage?.totalCount() ?: 0
    val checkedCount: Int get() = checked.size
    val progress: Float get() = if (totalCount == 0) 0f else checkedCount.toFloat() / totalCount
}

class ChecklistViewModel(
    private val months: Int,
    private val getStage: GetChecklistStageUseCase,
    private val calculateResult: CalculateResultUseCase,
    private val saveCheckRecord: SaveCheckRecordUseCase,
    private val appPreferences: AppPreferencesRepository,
    observeChildProfiles: ObserveChildProfilesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChecklistUiState())
    val state: StateFlow<ChecklistUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            observeChildProfiles()
                .combine(appPreferences.preferences) { profiles, prefs ->
                    profiles.resolveActive(prefs.activeChildId)
                }
                .collect { active ->
                    _state.update { it.copy(activeChild = active) }
                }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val stage = getStage(months)
                _state.update { it.copy(stage = stage, loading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = t.message ?: "로드 실패") }
            }
        }
    }

    fun toggle(itemId: String) {
        _state.update { current ->
            val newChecked = current.checked.toMutableSet().apply {
                if (!add(itemId)) remove(itemId)
            }
            current.copy(checked = newChecked)
        }
    }

    fun showResult() {
        val current = _state.value
        val stage = current.stage ?: return
        val result = calculateResult(stage, current.checked)
        _state.update { it.copy(result = result, resultSaved = false) }

        val activeChildId = current.activeChild?.id
        viewModelScope.launch {
            runCatching { saveCheckRecord(result, activeChildId) }
                .onSuccess {
                    _state.update { it.copy(resultSaved = true) }
                }
        }
    }

    fun dismissResult() {
        _state.update { it.copy(result = null, resultSaved = false) }
    }
}
