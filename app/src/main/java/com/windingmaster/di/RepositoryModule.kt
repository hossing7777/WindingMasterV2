package com.windingmaster.di

import com.windingmaster.data.repository.CalculationRepositoryImpl
import com.windingmaster.data.repository.ChecklistRepositoryImpl
import com.windingmaster.data.repository.MotorRepositoryImpl
import com.windingmaster.data.repository.NoteRepositoryImpl
import com.windingmaster.domain.repository.CalculationRepository
import com.windingmaster.domain.repository.ChecklistRepository
import com.windingmaster.domain.repository.MotorRepository
import com.windingmaster.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindMotorRepository(impl: MotorRepositoryImpl): MotorRepository
    @Binds abstract fun bindChecklistRepository(impl: ChecklistRepositoryImpl): ChecklistRepository
    @Binds abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
    @Binds abstract fun bindCalculationRepository(impl: CalculationRepositoryImpl): CalculationRepository
}
