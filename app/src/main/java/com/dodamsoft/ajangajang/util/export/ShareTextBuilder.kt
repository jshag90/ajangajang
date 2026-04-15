package com.dodamsoft.ajangajang.util.export

import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaMeta

/**
 * Single source of truth for share text. Every outgoing share MUST go through this builder
 * so the app link footer is never accidentally dropped.
 */
object ShareTextBuilder {

    private const val APP_LINK_LINE = "아장아장 앱으로 확인하기\nhttps://ajangajang.app"

    fun build(result: CheckResult, childProfile: ChildProfile?): String {
        val name = childProfile?.name ?: "우리 아이"
        val age = childProfile?.ageDisplay() ?: "${result.stage.months}개월"
        val percent = (result.overallRatio * 100).toInt()
        val tier = result.overallTier.label

        val areaLines = result.areaScores.joinToString("\n") { score ->
            val pct = (score.ratio * 100).toInt()
            "${DevelopmentAreaMeta.labelOf(score.type)}  ${score.checked}/${score.total}  ${pct}%"
        }

        return buildString {
            appendLine("[아장아장 발달 체크 결과]")
            appendLine("$name · $age")
            appendLine()
            appendLine("전체 달성률: $percent% ($tier)")
            appendLine("충족 ${result.checkedIds.size} / 전체 ${result.stage.totalCount()}")
            appendLine()
            appendLine(areaLines)
            appendLine()
            appendLine("━━━━━━━━━━")
            append(APP_LINK_LINE)
        }
    }
}
