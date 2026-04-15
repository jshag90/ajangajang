package com.dodamsoft.ajangajang.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dodamsoft.ajangajang.data.local.entity.ChildProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {

    @Query("SELECT * FROM child_profile ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profile ORDER BY createdAt ASC")
    suspend fun getAll(): List<ChildProfileEntity>

    @Query("SELECT * FROM child_profile WHERE id = :id")
    suspend fun getById(id: Long): ChildProfileEntity?

    @Query("SELECT COUNT(*) FROM child_profile")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(profile: ChildProfileEntity): Long

    @Update
    suspend fun update(profile: ChildProfileEntity)

    @Delete
    suspend fun delete(profile: ChildProfileEntity)

    @Query("UPDATE child_profile SET isPrimary = (id = :id)")
    suspend fun setPrimary(id: Long)
}
