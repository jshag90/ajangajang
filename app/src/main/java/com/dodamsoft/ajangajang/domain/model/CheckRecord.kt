package com.dodamsoft.ajangajang.domain.model

import java.time.LocalDateTime

data class CheckRecord(
    val id: Long,
    val childId: Long?,
    val ageMonths: Int,
    val checkedAt: LocalDateTime,
    val socialScore: Float,
    val languageScore: Float,
    val cognitiveScore: Float,
    val physicalScore: Float,
    val overallRatio: Float,
    val tier: ResultTier,
    val checkedItemIds: Set<String>,
    val totalItems: Int,
    val checkedCount: Int,
)
