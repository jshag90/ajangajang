package com.dodamsoft.ajangajang.ui.checklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.ResultTier
import com.dodamsoft.ajangajang.ui.share.ShareActionSheet
import com.dodamsoft.ajangajang.ui.theme.StateDanger
import com.dodamsoft.ajangajang.ui.theme.StateSuccess
import com.dodamsoft.ajangajang.ui.theme.StateWarning

@Composable
fun ResultDialog(
    result: CheckResult,
    childProfile: ChildProfile?,
    onDismiss: () -> Unit,
) {
    var shareSheetOpen by remember { mutableStateOf(false) }
    val percentage = (result.overallRatio * 100).toInt()
    val tierColor = resultTierColor(result.overallTier)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("발달 체크 결과") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { result.overallRatio },
                        color = tierColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Text(
                        text = "$percentage%  (${result.checkedIds.size}/${result.stage.totalCount()} 항목)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = buildTierMessage(result.overallTier),
                    style = MaterialTheme.typography.bodyMedium,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = buildAnnotatedString {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("기다리지 마세요! ")
                            pop()
                            append("발달지표 미충족이나 우려사항이 있다면 바로 소아과 선생님과 상의해 주세요.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = { shareSheetOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.IosShare,
                    contentDescription = null,
                )
                Text(text = "  저장·공유")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
    )

    if (shareSheetOpen) {
        ShareActionSheet(
            result = result,
            childProfile = childProfile,
            onDismiss = { shareSheetOpen = false },
        )
    }
}

private fun buildTierMessage(tier: ResultTier): AnnotatedString {
    val (label, description) = when (tier) {
        ResultTier.NORMAL -> "정상 범위:" to " 대부분 아기가 도달합니다."
        ResultTier.CAUTION -> "주의 관찰:" to " 일부 지표 미도달, 소아과 상담 권장."
        ResultTier.CONSULT -> "전문의 상담 권장:" to " 여러 지표 미도달, 성장발달 검사 필요."
    }
    val color = resultTierColor(tier)
    return buildAnnotatedString {
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color))
        append(label)
        pop()
        append(description)
    }
}

fun resultTierColor(tier: ResultTier): Color = when (tier) {
    ResultTier.NORMAL -> StateSuccess
    ResultTier.CAUTION -> StateWarning
    ResultTier.CONSULT -> StateDanger
}
