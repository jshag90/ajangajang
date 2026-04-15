package com.dodamsoft.ajangajang.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dodamsoft.ajangajang.data.local.dao.CheckRecordDao
import com.dodamsoft.ajangajang.data.local.dao.ChildProfileDao
import com.dodamsoft.ajangajang.data.local.entity.CheckRecordEntity
import com.dodamsoft.ajangajang.data.local.entity.ChildProfileEntity

@Database(
    entities = [ChildProfileEntity::class, CheckRecordEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AjangDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun checkRecordDao(): CheckRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AjangDatabase? = null

        fun get(context: Context): AjangDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AjangDatabase::class.java,
                    "ajang.db",
                ).build().also { INSTANCE = it }
            }
    }
}
