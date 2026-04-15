package com.dodamsoft.ajangajang.ui.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.domain.usecase.GetAllStagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StageSummary(
    val months: Int,
    val label: String,
    val itemCount: Int,
)

data class StageSelectUiState(
    val stages: List<StageSummary> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

class StageSelectViewModel(
    private val getAllStages: GetAllStagesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(StageSelectUiState())
    val state: StateFlow<StageSelectUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val summaries = getAllStages().map { stage ->
                    StageSummary(
                        months = stage.months,
                        label = stage.displayLabel,
                        itemCount = stage.totalCount(),
                    )
                }
                _state.update { it.copy(stages = summaries, loading = false) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = t.message ?: "로드 실패") }
            }
        }
    }
}
