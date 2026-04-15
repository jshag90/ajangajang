package com.dodamsoft.ajangajang.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.usecase.CalculateResultUseCase
import com.dodamsoft.ajangajang.domain.usecase.GetChecklistStageUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveAllCheckRecordsUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth

data class MonthGroup(
    val month: YearMonth,
    val records: List<CheckRecord>,
)

data class TrendSeries(
    val points: List<Pair<Int, Float>>,  // (ageMonths, ratio)
)

data class RecordsUiState(
    val groups: List<MonthGroup> = emptyList(),
    val profiles: List<ChildProfile> = emptyList(),
    val loading: Boolean = true,
    val openResult: CheckResult? = null,
    val openChild: ChildProfile? = null,
    val trend: TrendSeries? = null,
) {
    val hasTrend: Boolean get() = (trend?.points?.size ?: 0) >= 2
}

class RecordsViewModel(
    observeAll: ObserveAllCheckRecordsUseCase,
    observeChildProfiles: ObserveChildProfilesUseCase,
    private val getStage: GetChecklistStageUseCase,
    private val calculateResult: CalculateResultUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsUiState())
    val state: StateFlow<RecordsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAll().combine(observeChildProfiles()) { records, profiles -> records to profiles }
                .collect { (records, profiles) ->
                    val grouped = records
                        .groupBy { YearMonth.from(it.checkedAt) }
                        .toSortedMap(compareByDescending { it })
                        .map { (month, list) ->
                            MonthGroup(month, list.sortedByDescending { it.checkedAt })
                        }
                    val trendPoints = records
                        .sortedBy { it.ageMonths }
                        .map { it.ageMonths to it.overallRatio }
                    _state.update {
                        it.copy(
                            groups = grouped,
                            profiles = profiles,
                            loading = false,
                            trend = if (trendPoints.size >= 2) TrendSeries(trendPoints) else null,
                        )
                    }
                }
        }
    }

    fun openRecord(record: CheckRecord) {
        viewModelScope.launch {
            runCatching {
                val stage = getStage(record.ageMonths)
                val result = calculateResult(stage, record.checkedItemIds)
                val child = record.childId?.let { id -> _state.value.profiles.firstOrNull { it.id == id } }
                _state.update { it.copy(openResult = result, openChild = child) }
            }
        }
    }

    fun closeResult() {
        _state.update { it.copy(openResult = null, openChild = null) }
    }
}
