package com.dodamsoft.ajangajang.domain.usecase

import com.dodamsoft.ajangajang.domain.model.ChecklistStage
import com.dodamsoft.ajangajang.domain.repository.ChecklistRepository

class GetAllStagesUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(): List<ChecklistStage> = repository.getAllStages()
}

class GetChecklistStageUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(months: Int): ChecklistStage =
        repository.getStage(months) ?: error("Unknown stage: $months")
}
