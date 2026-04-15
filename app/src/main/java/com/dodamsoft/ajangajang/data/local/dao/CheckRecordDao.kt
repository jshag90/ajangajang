package com.dodamsoft.ajangajang.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dodamsoft.ajangajang.data.local.entity.CheckRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckRecordDao {

    @Query("SELECT * FROM check_record ORDER BY checkedAt DESC")
    fun observeAll(): Flow<List<CheckRecordEntity>>

    @Query("SELECT * FROM check_record WHERE childId = :childId OR childId IS NULL ORDER BY checkedAt DESC")
    fun observeForChildOrGuest(childId: Long): Flow<List<CheckRecordEntity>>

    @Query("SELECT * FROM check_record WHERE childId IS NULL ORDER BY checkedAt DESC")
    fun observeGuest(): Flow<List<CheckRecordEntity>>

    @Query("SELECT * FROM check_record ORDER BY checkedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<CheckRecordEntity>

    @Query("SELECT COUNT(*) FROM check_record")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: CheckRecordEntity): Long

    @Query("UPDATE check_record SET childId = :childId WHERE childId IS NULL")
    suspend fun attributeGuestRecordsTo(childId: Long): Int

    @Query("DELETE FROM check_record WHERE id = :id")
    suspend fun deleteById(id: Long)
}
