package com.dodamsoft.ajangajang.domain.usecase

import com.dodamsoft.ajangajang.domain.model.AreaScore
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChecklistItem
import com.dodamsoft.ajangajang.domain.model.ChecklistStage
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.domain.model.ResultTier

class CalculateResultUseCase {
    operator fun invoke(stage: ChecklistStage, checkedIds: Set<String>): CheckResult {
        val areaScores = stage.areas.map { area ->
            val hits = area.items.count { it.id in checkedIds }
            AreaScore(type = area.type, checked = hits, total = area.items.size)
        }
        val totalChecked = areaScores.sumOf { it.checked }
        val total = areaScores.sumOf { it.total }
        val ratio = if (total == 0) 0f else totalChecked.toFloat() / total
        val unfulfilled: List<Pair<DevelopmentAreaType, ChecklistItem>> =
            stage.areas.flatMap { area ->
                area.items.filter { it.id !in checkedIds }.map { area.type to it }
            }
        return CheckResult(
            stage = stage,
            checkedIds = checkedIds,
            areaScores = areaScores,
            overallRatio = ratio,
            overallTier = ResultTier.of(ratio),
            unfulfilledItems = unfulfilled,
        )
    }
}
