package com.dodamsoft.ajangajang.domain.model

import java.time.LocalDate
import java.time.Period

enum class Gender { MALE, FEMALE, UNKNOWN }

data class ChildProfile(
    val id: Long,
    val name: String,
    val birthDate: LocalDate,
    val gender: Gender,
    val photoUri: String? = null,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun ageMonthsOn(today: LocalDate = LocalDate.now()): Int {
        val period = Period.between(birthDate, today)
        return period.years * 12 + period.months
    }

    fun ageDisplay(today: LocalDate = LocalDate.now()): String {
        val months = ageMonthsOn(today)
        return when {
            months < 24 -> "${months}개월"
            else -> "${months / 12}세 ${months % 12}개월"
        }
    }
}

/**
 * Fallback chain: explicit active id → primary flag → first profile.
 * Single source of truth for "which child is currently being viewed."
 */
fun List<ChildProfile>.resolveActive(activeChildId: Long?): ChildProfile? =
    firstOrNull { it.id == activeChildId }
        ?: firstOrNull { it.isPrimary }
        ?: firstOrNull()
