package com.dodamsoft.ajangajang.domain.model

enum class ResultTier(val minRatio: Float, val label: String) {
    NORMAL(0.75f, "정상 범위"),
    CAUTION(0.50f, "주의 관찰"),
    CONSULT(0f, "전문의 상담 권장");

    companion object {
        fun of(ratio: Float): ResultTier = values().first { ratio >= it.minRatio }
    }
}

data class AreaScore(
    val type: DevelopmentAreaType,
    val checked: Int,
    val total: Int,
) {
    val ratio: Float get() = if (total == 0) 0f else checked.toFloat() / total
}

data class CheckResult(
    val stage: ChecklistStage,
    val checkedIds: Set<String>,
    val areaScores: List<AreaScore>,
    val overallRatio: Float,
    val overallTier: ResultTier,
    val unfulfilledItems: List<Pair<DevelopmentAreaType, ChecklistItem>>,
)
