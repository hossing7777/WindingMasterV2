package com.windingmaster.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "motors")
data class MotorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val manufacturer: String,
    val powerKw: Double?,
    val voltage: Int?,
    val currentA: Double?,
    val rpm: Int?,
    val phases: Int,
    val slots: Int?,
    val poles: Int?,
    val connection: String,
    val wireDiameterMm: Double?,
    val turnsPerCoil: Int?,
    val notes: String,
    val createdAt: Long
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [ForeignKey(
        entity = MotorEntity::class,
        parentColumns = ["id"],
        childColumns = ["motorId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("motorId")]
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motorId: Long?,
    val templateKey: String?,
    val customText: String?,
    val isDone: Boolean,
    val sortOrder: Int
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = MotorEntity::class,
        parentColumns = ["id"],
        childColumns = ["motorId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("motorId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val motorId: Long?,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "saved_calculations")
data class SavedCalculationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slots: Int,
    val poles: Int,
    val frequency: Int,
    val phases: Int,
    val windingType: String,
    val coilPitch: Int?,
    val createdAt: Long
)
