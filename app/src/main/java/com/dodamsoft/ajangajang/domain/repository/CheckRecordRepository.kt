package com.dodamsoft.ajangajang.domain.repository

import com.dodamsoft.ajangajang.domain.model.CheckRecord
import kotlinx.coroutines.flow.Flow

interface CheckRecordRepository {
    fun observeAll(): Flow<List<CheckRecord>>
    fun observeForChildOrGuest(childId: Long): Flow<List<CheckRecord>>
    fun observeGuest(): Flow<List<CheckRecord>>
    suspend fun recent(limit: Int): List<CheckRecord>
    suspend fun count(): Int
    suspend fun insert(record: CheckRecord): Long
    suspend fun attributeGuestRecordsTo(childId: Long): Int
    suspend fun deleteById(id: Long)
}
