package com.windingmaster.domain.repository

import com.windingmaster.domain.model.ChecklistItem
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.model.Note
import com.windingmaster.domain.model.SavedCalculation
import kotlinx.coroutines.flow.Flow

interface MotorRepository {
    fun observeAll(): Flow<List<MotorProfile>>
    suspend fun getById(id: Long): MotorProfile?
    suspend fun save(motor: MotorProfile): Long
    suspend fun delete(id: Long)
}

interface ChecklistRepository {
    fun observe(motorId: Long?): Flow<List<ChecklistItem>>
    suspend fun addCustom(motorId: Long?, text: String)
    suspend fun setDone(id: Long, done: Boolean)
    suspend fun delete(id: Long)
    suspend fun resetAll(motorId: Long?)
    suspend fun seedIfEmpty(motorId: Long?, templateKeys: List<String>)
}

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>
    suspend fun getById(id: Long): Note?
    suspend fun save(note: Note): Long
    suspend fun delete(id: Long)
}

interface CalculationRepository {
    fun observeAll(): Flow<List<SavedCalculation>>
    suspend fun save(calc: SavedCalculation): Long
    suspend fun delete(id: Long)
}
