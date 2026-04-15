package com.dodamsoft.ajangajang.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dodamsoft.ajangajang.domain.model.CheckResult
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaMeta
import com.dodamsoft.ajangajang.domain.model.DevelopmentAreaType
import com.dodamsoft.ajangajang.ui.charts.RadarChart4
import com.dodamsoft.ajangajang.util.export.MediaStoreSaver
import com.dodamsoft.ajangajang.util.export.ResultImageExporter
import com.dodamsoft.ajangajang.util.export.ResultPdfExporter
import com.dodamsoft.ajangajang.util.export.SavedFile
import com.dodamsoft.ajangajang.util.export.ShareLauncher
import com.dodamsoft.ajangajang.util.export.ShareTextBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private sealed interface ShareSheetStatus {
    data object Idle : ShareSheetStatus
    data class Working(val action: ActionKind) : ShareSheetStatus
    data class Saved(val file: SavedFile) : ShareSheetStatus
    data class Error(val message: String) : ShareSheetStatus
}

private enum class ActionKind { Pdf, Image, Share }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareActionSheet(
    result: CheckResult,
    childProfile: ChildProfile?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var status: ShareSheetStatus by remember { mutableStateOf(ShareSheetStatus.Idle) }

    val dateStamp = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) }
    val baseName = remember(childProfile, result) {
        val n = childProfile?.name ?: "우리아이"
        "아장아장_${n}_${result.stage.months}개월_$dateStamp"
    }

    fun runSavePdf() {
        status = ShareSheetStatus.Working(ActionKind.Pdf)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val pdf = ResultPdfExporter.render(context, result, childProfile)
                    try {
                        MediaStoreSaver.savePdf(context, pdf, "$baseName.pdf")
                    } finally {
                        pdf.close()
                    }
                }
            }.onSuccess { status = ShareSheetStatus.Saved(it) }
                .onFailure { status = ShareSheetStatus.Error(it.message ?: "PDF 저장 실패") }
        }
    }

    fun runSaveImage() {
        status = ShareSheetStatus.Working(ActionKind.Image)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val bitmap = ResultImageExporter.render(context, result, childProfile)
                    try {
                        MediaStoreSaver.saveImage(context, bitmap, "$baseName.png")
                    } finally {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    }
                }
            }.onSuccess { status = ShareSheetStatus.Saved(it) }
                .onFailure { status = ShareSheetStatus.Error(it.message ?: "이미지 저장 실패") }
        }
    }

    fun runShare() {
        status = ShareSheetStatus.Working(ActionKind.Share)
        scope.launch {
            runCatching {
                val shareText = ShareTextBuilder.build(result, childProfile)
                withContext(Dispatchers.IO) {
                    val bitmap = ResultImageExporter.render(context, result, childProfile)
                    try {
                        val uri = MediaStoreSaver.cacheImageForShare(context, bitmap, "$baseName.png")
                        uri to shareText
                    } finally {
                        if (!bitmap.isRecycled) bitmap.recycle()
                    }
                }
            }.onSuccess { (uri, text) ->
                ShareLauncher.shareImage(context, uri, text)
                status = ShareSheetStatus.Idle
            }.onFailure { status = ShareSheetStatus.Error(it.message ?: "공유 실패") }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "저장 · 공유",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 4.dp),
            )
            Text(
                text = "체크 결과를 파일로 저장하거나 다른 사람에게 공유할 수 있어요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )

            RadarPreview(result = result)

            val workingKind = (status as? ShareSheetStatus.Working)?.action

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ActionCard(
                    icon = Icons.Filled.PictureAsPdf,
                    label = "PDF 저장",
                    loading = workingKind == ActionKind.Pdf,
                    enabled = status !is ShareSheetStatus.Working,
                    onClick = ::runSavePdf,
                    modifier = Modifier.weight(1f),
                )
                ActionCard(
                    icon = Icons.Filled.Image,
                    label = "이미지 저장",
                    loading = workingKind == ActionKind.Image,
                    enabled = status !is ShareSheetStatus.Working,
                    onClick = ::runSaveImage,
                    modifier = Modifier.weight(1f),
                )
                ActionCard(
                    icon = Icons.Filled.Share,
                    label = "공유하기",
                    loading = workingKind == ActionKind.Share,
                    enabled = status !is ShareSheetStatus.Working,
                    onClick = ::runShare,
                    modifier = Modifier.weight(1f),
                )
            }

            when (val s = status) {
                is ShareSheetStatus.Saved -> SavedBanner(
                    file = s.file,
                    onOpen = {
                        ShareLauncher.openSaved(context, s.file.uri, s.file.mimeType)
                    },
                    onShare = {
                        val text = ShareTextBuilder.build(result, childProfile)
                        if (s.file.mimeType == "application/pdf") {
                            ShareLauncher.sharePdf(context, s.file.uri, text)
                        } else {
                            ShareLauncher.shareImage(context, s.file.uri, text)
                        }
                    },
                )
                is ShareSheetStatus.Error -> ErrorBanner(s.message)
                else -> Unit
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("닫기")
            }
        }
    }
}

@Composable
private fun RadarPreview(result: CheckResult) {
    val byType = result.areaScores.associateBy { it.type }
    val values = floatArrayOf(
        byType[DevelopmentAreaType.SOCIAL]?.ratio ?: 0f,
        byType[DevelopmentAreaType.LANGUAGE]?.ratio ?: 0f,
        byType[DevelopmentAreaType.COGNITIVE]?.ratio ?: 0f,
        byType[DevelopmentAreaType.PHYSICAL]?.ratio ?: 0f,
    )
    val labels = listOf(
        DevelopmentAreaMeta.labelOf(DevelopmentAreaType.SOCIAL),
        DevelopmentAreaMeta.labelOf(DevelopmentAreaType.LANGUAGE),
        DevelopmentAreaMeta.labelOf(DevelopmentAreaType.COGNITIVE),
        DevelopmentAreaMeta.labelOf(DevelopmentAreaType.PHYSICAL),
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "영역별 도달률 미리보기",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RadarChart4(
                values = values,
                labels = labels,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    label: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun SavedBanner(
    file: SavedFile,
    onOpen: () -> Unit,
    onShare: () -> Unit,
) {
    val msg = if (file.mimeType == "application/pdf") {
        "PDF가 저장되었어요"
    } else {
        "이미지가 저장되었어요"
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = msg,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpen) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "  열기")
                }
                TextButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "  공유")
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Column {
                Text(
                    text = "문제가 생겼어요",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
