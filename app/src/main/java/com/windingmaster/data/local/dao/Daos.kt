package com.windingmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.windingmaster.data.local.entity.ChecklistItemEntity
import com.windingmaster.data.local.entity.MotorEntity
import com.windingmaster.data.local.entity.NoteEntity
import com.windingmaster.data.local.entity.SavedCalculationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotorDao {
    @Query("SELECT * FROM motors ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MotorEntity>>

    @Query("SELECT * FROM motors WHERE id = :id")
    suspend fun getById(id: Long): MotorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MotorEntity): Long

    @Query("DELETE FROM motors WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ChecklistDao {
    @Query(
        "SELECT * FROM checklist_items " +
        "WHERE (:motorId IS NULL AND motorId IS NULL) OR motorId = :motorId " +
        "ORDER BY sortOrder, id"
    )
    fun observe(motorId: Long?): Flow<List<ChecklistItemEntity>>

    @Query(
        "SELECT COUNT(*) FROM checklist_items " +
        "WHERE (:motorId IS NULL AND motorId IS NULL) OR motorId = :motorId"
    )
    suspend fun count(motorId: Long?): Int

    @Query(
        "SELECT COALESCE(MAX(sortOrder), 0) FROM checklist_items " +
        "WHERE (:motorId IS NULL AND motorId IS NULL) OR motorId = :motorId"
    )
    suspend fun maxOrder(motorId: Long?): Int

    @Insert
    suspend fun insert(entity: ChecklistItemEntity): Long

    @Insert
    suspend fun insertAll(entities: List<ChecklistItemEntity>)

    @Query("UPDATE checklist_items SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        "UPDATE checklist_items SET isDone = 0 " +
        "WHERE (:motorId IS NULL AND motorId IS NULL) OR motorId = :motorId"
    )
    suspend fun resetAll(motorId: Long?)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface CalculationDao {
    @Query("SELECT * FROM saved_calculations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedCalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedCalculationEntity): Long

    @Query("DELETE FROM saved_calculations WHERE id = :id")
    suspend fun delete(id: Long)
}
