package com.windingmaster.di

import android.content.Context
import androidx.room.Room
import com.windingmaster.data.local.AppDatabase
import com.windingmaster.data.local.dao.CalculationDao
import com.windingmaster.data.local.dao.ChecklistDao
import com.windingmaster.data.local.dao.MotorDao
import com.windingmaster.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides fun provideMotorDao(db: AppDatabase): MotorDao = db.motorDao()
    @Provides fun provideChecklistDao(db: AppDatabase): ChecklistDao = db.checklistDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideCalculationDao(db: AppDatabase): CalculationDao = db.calculationDao()
}
