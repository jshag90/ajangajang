package com.dodamsoft.ajangajang.data.repository

import com.dodamsoft.ajangajang.data.local.dao.CheckRecordDao
import com.dodamsoft.ajangajang.data.local.entity.CheckRecordEntity
import com.dodamsoft.ajangajang.domain.model.CheckRecord
import com.dodamsoft.ajangajang.domain.model.ResultTier
import com.dodamsoft.ajangajang.domain.repository.CheckRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class RoomCheckRecordRepository(
    private val dao: CheckRecordDao,
) : CheckRecordRepository {

    override fun observeAll(): Flow<List<CheckRecord>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeForChildOrGuest(childId: Long): Flow<List<CheckRecord>> =
        dao.observeForChildOrGuest(childId).map { list -> list.map { it.toDomain() } }

    override fun observeGuest(): Flow<List<CheckRecord>> =
        dao.observeGuest().map { list -> list.map { it.toDomain() } }

    override suspend fun recent(limit: Int): List<CheckRecord> =
        dao.recent(limit).map { it.toDomain() }

    override suspend fun count(): Int = dao.count()

    override suspend fun insert(record: CheckRecord): Long =
        dao.insert(record.toEntity())

    override suspend fun attributeGuestRecordsTo(childId: Long): Int =
        dao.attributeGuestRecordsTo(childId)

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}

private fun CheckRecordEntity.toDomain(): CheckRecord = CheckRecord(
    id = id,
    childId = childId,
    ageMonths = ageMonths,
    checkedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(checkedAt), ZoneId.systemDefault()),
    socialScore = socialScore,
    languageScore = languageScore,
    cognitiveScore = cognitiveScore,
    physicalScore = physicalScore,
    overallRatio = overallRatio,
    tier = runCatching { ResultTier.valueOf(tier) }.getOrDefault(ResultTier.CONSULT),
    checkedItemIds = if (checkedItemsCsv.isBlank()) emptySet() else checkedItemsCsv.split(",").toSet(),
    totalItems = totalItems,
    checkedCount = checkedCount,
)

private fun CheckRecord.toEntity(): CheckRecordEntity = CheckRecordEntity(
    id = id,
    childId = childId,
    ageMonths = ageMonths,
    checkedAt = checkedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    socialScore = socialScore,
    languageScore = languageScore,
    cognitiveScore = cognitiveScore,
    physicalScore = physicalScore,
    overallRatio = overallRatio,
    tier = tier.name,
    checkedItemsCsv = checkedItemIds.joinToString(","),
    totalItems = totalItems,
    checkedCount = checkedCount,
)
