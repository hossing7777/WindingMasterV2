package com.windingmaster.data.repository

import com.windingmaster.data.local.dao.CalculationDao
import com.windingmaster.data.local.dao.ChecklistDao
import com.windingmaster.data.local.dao.MotorDao
import com.windingmaster.data.local.dao.NoteDao
import com.windingmaster.data.local.entity.ChecklistItemEntity
import com.windingmaster.domain.model.ChecklistItem
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.model.Note
import com.windingmaster.domain.model.SavedCalculation
import com.windingmaster.domain.repository.CalculationRepository
import com.windingmaster.domain.repository.ChecklistRepository
import com.windingmaster.domain.repository.MotorRepository
import com.windingmaster.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MotorRepositoryImpl @Inject constructor(
    private val dao: MotorDao
) : MotorRepository {
    override fun observeAll(): Flow<List<MotorProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()
    override suspend fun save(motor: MotorProfile) = dao.upsert(motor.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class ChecklistRepositoryImpl @Inject constructor(
    private val dao: ChecklistDao
) : ChecklistRepository {
    override fun observe(motorId: Long?): Flow<List<ChecklistItem>> =
        dao.observe(motorId).map { list -> list.map { it.toDomain() } }

    override suspend fun addCustom(motorId: Long?, text: String) {
        val order = dao.maxOrder(motorId) + 1
        dao.insert(
            ChecklistItemEntity(
                motorId = motorId, templateKey = null, customText = text.trim(),
                isDone = false, sortOrder = order
            )
        )
    }

    override suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done)
    override suspend fun delete(id: Long) = dao.delete(id)
    override suspend fun resetAll(motorId: Long?) = dao.resetAll(motorId)

    override suspend fun seedIfEmpty(motorId: Long?, templateKeys: List<String>) {
        if (dao.count(motorId) > 0) return
        dao.insertAll(
            templateKeys.mapIndexed { index, key ->
                ChecklistItemEntity(
                    motorId = motorId, templateKey = key, customText = null,
                    isDone = false, sortOrder = index
                )
            }
        )
    }
}

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {
    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()

    override suspend fun save(note: Note) =
        dao.upsert(note.copy(updatedAt = System.currentTimeMillis()).toEntity())

    override suspend fun delete(id: Long) = dao.delete(id)
}

@Singleton
class CalculationRepositoryImpl @Inject constructor(
    private val dao: CalculationDao
) : CalculationRepository {
    override fun observeAll(): Flow<List<SavedCalculation>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(calc: SavedCalculation) = dao.upsert(calc.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
}
