package com.dodamsoft.ajangajang.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DevelopmentAreaType { SOCIAL, LANGUAGE, COGNITIVE, PHYSICAL }

object DevelopmentAreaMeta {
    fun labelOf(type: DevelopmentAreaType): String = when (type) {
        DevelopmentAreaType.SOCIAL -> "사회적/감정적"
        DevelopmentAreaType.LANGUAGE -> "언어/의사소통"
        DevelopmentAreaType.COGNITIVE -> "인지력"
        DevelopmentAreaType.PHYSICAL -> "움직임/신체발달"
    }

    fun emojiOf(type: DevelopmentAreaType): String = when (type) {
        DevelopmentAreaType.SOCIAL -> "💛"
        DevelopmentAreaType.LANGUAGE -> "💬"
        DevelopmentAreaType.COGNITIVE -> "🧠"
        DevelopmentAreaType.PHYSICAL -> "🏃"
    }
}

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
)

@Serializable
data class DevelopmentArea(
    val type: DevelopmentAreaType,
    val items: List<ChecklistItem>,
)

@Serializable
data class GrowthTip(
    val title: String? = null,
    val body: String,
)

@Serializable
data class ChecklistStage(
    val months: Int,
    val displayLabel: String,
    val areas: List<DevelopmentArea>,
    val doctorQuestions: List<String> = emptyList(),
    val growthTips: List<GrowthTip> = emptyList(),
) {
    fun totalCount(): Int = areas.sumOf { it.items.size }
}

@Serializable
data class ChecklistCatalog(
    val version: Int,
    val stages: List<ChecklistStage>,
)
