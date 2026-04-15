package com.dodamsoft.ajangajang.domain.usecase

import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.domain.repository.CheckRecordRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class SaveCheckRecordUseCase(
    private val repository: CheckRecordRepository,
) {
    suspend operator fun invoke(
        result: CheckResult,
        childId: Long?,
    ): Long {
        val byType = result.areaScores.associateBy { it.type }
        val record = CheckRecord(
            id = 0L,
            childId = childId,
            ageMonths = result.stage.months,
            checkedAt = LocalDateTime.now(),
            socialScore = byType[DevelopmentAreaType.SOCIAL]?.ratio ?: 0f,
            languageScore = byType[DevelopmentAreaType.LANGUAGE]?.ratio ?: 0f,
            cognitiveScore = byType[DevelopmentAreaType.COGNITIVE]?.ratio ?: 0f,
            physicalScore = byType[DevelopmentAreaType.PHYSICAL]?.ratio ?: 0f,
            overallRatio = result.overallRatio,
            tier = result.overallTier,
            checkedItemIds = result.checkedIds,
            totalItems = result.stage.totalCount(),
            checkedCount = result.checkedIds.size,
        )
        return repository.insert(record)
    }
}

class ObserveAllCheckRecordsUseCase(
    private val repository: CheckRecordRepository,
) {
    operator fun invoke(): Flow<List<CheckRecord>> = repository.observeAll()
}

class GetRecentCheckRecordsUseCase(
    private val repository: CheckRecordRepository,
) {
    suspend operator fun invoke(limit: Int = 3): List<CheckRecord> = repository.recent(limit)
}
