package com.dodamsoft.ajangajang.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_record",
    foreignKeys = [
        ForeignKey(
            entity = ChildProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("childId"), Index("checkedAt")],
)
data class CheckRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val childId: Long? = null,
    val ageMonths: Int,
    val checkedAt: Long,
    val socialScore: Float,
    val languageScore: Float,
    val cognitiveScore: Float,
    val physicalScore: Float,
    val overallRatio: Float,
    val tier: String,
    val checkedItemsCsv: String,
    val totalItems: Int,
    val checkedCount: Int,
)
