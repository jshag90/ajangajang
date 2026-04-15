package com.dodamsoft.ajangajang.ui.checklist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dodamsoft.ajangajang.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistStageSelectScreen(
    onStageClick: (months: Int) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: StageSelectViewModel = viewModel(factory = AppContainer.stageSelectVmFactory()),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "개월 수 선택",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error ?: "오류",
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                else -> StageGrid(
                    stages = state.stages,
                    onStageClick = onStageClick,
                )
            }
        }
    }
}

@Composable
private fun StageGrid(
    stages: List<StageSummary>,
    onStageClick: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            HeroHeader()
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                text = "우리 아이 개월 수를 골라보세요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp),
            )
        }
        items(stages, key = { it.months }) { stage ->
            StageCard(stage = stage, onClick = { onStageClick(stage.months) })
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            HelpNote()
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroHeader() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🐣", style = MaterialTheme.typography.displaySmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "우리 아이 발달체크",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "매일 조금씩, 아장아장 자라나요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun StageCard(
    stage: StageSummary,
    onClick: () -> Unit,
) {
    val palette = stagePalette(stage.months)
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = palette.background,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 상단 좌측: 아이콘
            Box(
                modifier = Modifier
                    .padding(14.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = palette.emoji,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            // 상단 우측: 특성 태그 pill
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(999.dp),
                color = palette.iconBg,
            ) {
                Text(
                    text = palette.tag,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textStrong,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            // 하단: 개월수 + 체크 개수
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stage.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textStrong,
                )
                Text(
                    text = "체크 ${stage.itemCount}개",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textSoft,
                )
            }
        }
    }
}

@Composable
private fun HelpNote() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "💡", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "개월 수가 사이에 있다면 가까운 개월 수로 확인해 보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

private data class StagePalette(
    val emoji: String,
    val tag: String,
    val background: Color,
    val iconBg: Color,
    val textStrong: Color,
    val textSoft: Color,
)

private fun stagePalette(months: Int): StagePalette = when (months) {
    2 -> StagePalette(
        emoji = "🌸",
        tag = "방긋 미소",
        background = Color(0xFFFFF0F5),
        iconBg = Color(0xFFFFCDD6),
        textStrong = Color(0xFF7A2D3F),
        textSoft = Color(0xFFA14E63),
    )
    4 -> StagePalette(
        emoji = "🎵",
        tag = "옹알옹알",
        background = Color(0xFFFFF4E6),
        iconBg = Color(0xFFFFDAB0),
        textStrong = Color(0xFF7A4510),
        textSoft = Color(0xFF9E6528),
    )
    6 -> StagePalette(
        emoji = "🍓",
        tag = "뒤집기",
        background = Color(0xFFFFEBEE),
        iconBg = Color(0xFFFFBCC4),
        textStrong = Color(0xFF8B1A2E),
        textSoft = Color(0xFFAE4355),
    )
    9 -> StagePalette(
        emoji = "🧩",
        tag = "기고 잡기",
        background = Color(0xFFFFF8E1),
        iconBg = Color(0xFFFFE082),
        textStrong = Color(0xFF6B4F00),
        textSoft = Color(0xFF8B6E1F),
    )
    12 -> StagePalette(
        emoji = "🎂",
        tag = "첫 걸음",
        background = Color(0xFFFDECE5),
        iconBg = Color(0xFFFACCB5),
        textStrong = Color(0xFF7A381A),
        textSoft = Color(0xFF9F5935),
    )
    15 -> StagePalette(
        emoji = "👣",
        tag = "아장아장",
        background = Color(0xFFE3F4EA),
        iconBg = Color(0xFFBCE6CC),
        textStrong = Color(0xFF1F5A3C),
        textSoft = Color(0xFF3E7A5A),
    )
    18 -> StagePalette(
        emoji = "🗣️",
        tag = "한두 단어",
        background = Color(0xFFE0F7FA),
        iconBg = Color(0xFFA8E1E8),
        textStrong = Color(0xFF0F4F5A),
        textSoft = Color(0xFF326F7A),
    )
    24 -> StagePalette(
        emoji = "🎈",
        tag = "두 단어 문장",
        background = Color(0xFFE6F1FF),
        iconBg = Color(0xFFBFDBFF),
        textStrong = Color(0xFF204275),
        textSoft = Color(0xFF41649A),
    )
    30 -> StagePalette(
        emoji = "🖍️",
        tag = "그리기 시작",
        background = Color(0xFFF3E5F5),
        iconBg = Color(0xFFDEB5E2),
        textStrong = Color(0xFF4A1850),
        textSoft = Color(0xFF6B3770),
    )
    36 -> StagePalette(
        emoji = "🎨",
        tag = "상상 놀이",
        background = Color(0xFFF0E9FE),
        iconBg = Color(0xFFD4C3FB),
        textStrong = Color(0xFF433080),
        textSoft = Color(0xFF63509D),
    )
    48 -> StagePalette(
        emoji = "🚲",
        tag = "친구 놀이",
        background = Color(0xFFE1F5EE),
        iconBg = Color(0xFFA8DFC7),
        textStrong = Color(0xFF0F4F38),
        textSoft = Color(0xFF306F5A),
    )
    60 -> StagePalette(
        emoji = "🎒",
        tag = "유치원 준비",
        background = Color(0xFFFFF4DC),
        iconBg = Color(0xFFFFD98C),
        textStrong = Color(0xFF6B4200),
        textSoft = Color(0xFF8F6500),
    )
    else -> StagePalette(
        emoji = "⭐",
        tag = "발달 체크",
        background = Color(0xFFF5F5F5),
        iconBg = Color(0xFFE0E0E0),
        textStrong = Color(0xFF424242),
        textSoft = Color(0xFF757575),
    )
}

