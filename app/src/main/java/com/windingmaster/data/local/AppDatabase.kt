package com.windingmaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.windingmaster.data.local.dao.CalculationDao
import com.windingmaster.data.local.dao.ChecklistDao
import com.windingmaster.data.local.dao.MotorDao
import com.windingmaster.data.local.dao.NoteDao
import com.windingmaster.data.local.entity.ChecklistItemEntity
import com.windingmaster.data.local.entity.MotorEntity
import com.windingmaster.data.local.entity.NoteEntity
import com.windingmaster.data.local.entity.SavedCalculationEntity

@Database(
    entities = [
        MotorEntity::class,
        ChecklistItemEntity::class,
        NoteEntity::class,
        SavedCalculationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun motorDao(): MotorDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun noteDao(): NoteDao
    abstract fun calculationDao(): CalculationDao

    companion object {
        const val NAME = "winding_master.db"
    }
}
