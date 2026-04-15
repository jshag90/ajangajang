package com.dodamsoft.ajangajang.domain.repository

import com.dodamsoft.ajangajang.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

interface ChildProfileRepository {
    fun observeAll(): Flow<List<ChildProfile>>
    suspend fun getAll(): List<ChildProfile>
    suspend fun getById(id: Long): ChildProfile?
    suspend fun count(): Int
    suspend fun insert(profile: ChildProfile): Long
    suspend fun update(profile: ChildProfile)
    suspend fun delete(profile: ChildProfile)
    suspend fun setPrimary(id: Long)
}
