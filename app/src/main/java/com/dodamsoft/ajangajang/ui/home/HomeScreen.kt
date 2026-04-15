package com.dodamsoft.ajangajang.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dodamsoft.ajangajang.di.AppContainer
import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.ResultTier
import com.dodamsoft.ajangajang.ui.checklist.resultTierColor
import com.dodamsoft.ajangajang.ui.profile.ProfileAddBottomSheet
import com.dodamsoft.ajangajang.util.koreanPossessive
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val availableMonths = listOf(2, 4, 6, 9, 12, 15, 18, 24, 30, 36, 48, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartChecklist: (Int) -> Unit,
    onOpenChecklistBrowser: () -> Unit,
    onOpenProfileManage: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppContainer.homeVmFactory()),
) {
    val state by viewModel.state.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "아장아장",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.isGuest) {
                guestHomeContent(
                    recentRecords = state.recentRecords,
                    onStartChecklist = onStartChecklist,
                    onOpenChecklistBrowser = onOpenChecklistBrowser,
                    onAddProfile = { showAddSheet = true },
                )
            } else {
                profileHomeContent(
                    profiles = state.profiles,
                    activeChild = state.activeChild,
                    onSelectChild = viewModel::selectChild,
                    onAddProfile = { showAddSheet = true },
                    onStartChecklist = onStartChecklist,
                    recentRecords = state.recentRecords,
                    onOpenProfileManage = onOpenProfileManage,
                )
            }
        }
    }

    if (showAddSheet) {
        ProfileAddBottomSheet(onDismiss = { showAddSheet = false })
    }
}

private fun LazyListScope.guestHomeContent(
    recentRecords: List<CheckRecord>,
    onStartChecklist: (Int) -> Unit,
    onOpenChecklistBrowser: () -> Unit,
    onAddProfile: () -> Unit,
) {
    item {
        Text(
            text = "안녕하세요",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "오늘도 함께해요",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
    item {
        AgePickerHeroCard(
            onSelectMonths = onStartChecklist,
            onSeeAll = onOpenChecklistBrowser,
        )
    }
    if (recentRecords.isNotEmpty()) {
        item {
            Text(
                text = "최근 체크",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            RecentRecordStrip(recentRecords.first())
        }
    }
    item { SoftProfileBanner(onClick = onAddProfile) }
    item {
        FaqCard()
    }
    item { Spacer(Modifier.height(12.dp)) }
}

private fun LazyListScope.profileHomeContent(
    profiles: List<ChildProfile>,
    activeChild: ChildProfile?,
    onSelectChild: (Long) -> Unit,
    onAddProfile: () -> Unit,
    onStartChecklist: (Int) -> Unit,
    recentRecords: List<CheckRecord>,
    onOpenProfileManage: () -> Unit,
) {
    item {
        ChildPager(
            profiles = profiles,
            activeId = activeChild?.id,
            onSelect = onSelectChild,
            onAdd = onAddProfile,
            onManage = onOpenProfileManage,
        )
    }
    item {
        val months = activeChild?.ageMonthsOn(LocalDate.now()) ?: 12
        val nearestStage = nearestStageFor(months)
        PersonalizedHeroCard(
            childName = activeChild?.name ?: "우리 아이",
            currentMonths = months,
            stageMonths = nearestStage,
            onStart = { onStartChecklist(nearestStage) },
        )
    }
    if (recentRecords.isNotEmpty()) {
        item {
            Text(
                text = "최근 기록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        items(
            recentRecords,
            key = { it.id },
        ) { record ->
            RecentRecordRow(record = record)
        }
    }
    item { Spacer(Modifier.height(12.dp)) }
}

@Composable
private fun AgePickerHeroCard(
    onSelectMonths: (Int) -> Unit,
    onSeeAll: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "🐣  우리 아이 몇 개월인가요?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "개월 수를 고르면 바로 체크리스트로 이동해요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableMonths.forEach { months ->
                    AgeChip(months = months, onClick = { onSelectMonths(months) })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "전체 보기  →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun AgeChip(months: Int, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Text(
                text = "${months}개월",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ChildPager(
    profiles: List<ChildProfile>,
    activeId: Long?,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    onManage: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        profiles.forEach { profile ->
            val isActive = profile.id == (activeId ?: profiles.firstOrNull()?.id)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                onClick = { onSelect(profile.id) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "👶", style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = profile.ageDisplay(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (profiles.size < 3) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onAdd,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(text = "아이 추가", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.size(4.dp))
    }
}

@Composable
private fun PersonalizedHeroCard(
    childName: String,
    currentMonths: Int,
    stageMonths: Int,
    onStart: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "$childName${koreanPossessive(childName)} 오늘의 체크리스트",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "${stageMonths}개월 발달지표",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (currentMonths != stageMonths) {
                Text(
                    text = "생후 ${currentMonths}개월 · 가까운 단계로 안내해요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "체크리스트 시작",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RecentRecordStrip(record: CheckRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(resultTierColor(record.tier)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.ageMonths}개월 체크",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = record.checkedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${(record.overallRatio * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = resultTierColor(record.tier),
            )
        }
    }
}

@Composable
private fun RecentRecordRow(record: CheckRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(resultTierColor(record.tier).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${(record.overallRatio * 100).toInt()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = resultTierColor(record.tier),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.ageMonths}개월 체크",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = record.checkedAt.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SoftProfileBanner(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "👶", style = MaterialTheme.typography.headlineSmall)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "아이 정보 추가",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = "이름과 생년월일을 저장하면 기록이 쌓여요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun FaqCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "💡 자주 묻는 질문",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            FaqRow("체크리스트는 어디서 온 건가요?", "미국 CDC의 발달 이정표를 참고했어요.")
            FaqRow("아이 정보를 꼭 등록해야 하나요?", "아니요. 등록하지 않아도 모든 기능을 쓸 수 있어요.")
            FaqRow("기록은 어디에 저장되나요?", "기기 안에만 저장돼요 (로컬 DB).")
        }
    }
}

@Composable
private fun FaqRow(q: String, a: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Q. $q",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "A. $a",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun nearestStageFor(months: Int): Int {
    return availableMonths.minByOrNull { kotlin.math.abs(it - months) } ?: 12
}
