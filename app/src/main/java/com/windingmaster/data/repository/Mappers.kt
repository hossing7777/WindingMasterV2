package com.windingmaster.data.repository

import com.windingmaster.data.local.entity.ChecklistItemEntity
import com.windingmaster.data.local.entity.MotorEntity
import com.windingmaster.data.local.entity.NoteEntity
import com.windingmaster.data.local.entity.SavedCalculationEntity
import com.windingmaster.domain.model.ChecklistItem
import com.windingmaster.domain.model.ConnectionType
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.model.Note
import com.windingmaster.domain.model.SavedCalculation
import com.windingmaster.domain.model.WindingType

private inline fun <reified T : Enum<T>> String.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default

fun MotorEntity.toDomain() = MotorProfile(
    id, name, manufacturer, powerKw, voltage, currentA, rpm, phases, slots, poles,
    connection.toEnumOr(ConnectionType.STAR), wireDiameterMm, turnsPerCoil, notes, createdAt
)

fun MotorProfile.toEntity() = MotorEntity(
    id, name, manufacturer, powerKw, voltage, currentA, rpm, phases, slots, poles,
    connection.name, wireDiameterMm, turnsPerCoil, notes, createdAt
)

fun ChecklistItemEntity.toDomain() =
    ChecklistItem(id, motorId, templateKey, customText, isDone, sortOrder)

fun ChecklistItem.toEntity() =
    ChecklistItemEntity(id, motorId, templateKey, customText, isDone, sortOrder)

fun NoteEntity.toDomain() = Note(id, motorId, title, body, createdAt, updatedAt)
fun Note.toEntity() = NoteEntity(id, motorId, title, body, createdAt, updatedAt)

fun SavedCalculationEntity.toDomain() = SavedCalculation(
    id, name, slots, poles, frequency, phases,
    windingType.toEnumOr(WindingType.FULL), coilPitch, createdAt
)

fun SavedCalculation.toEntity() = SavedCalculationEntity(
    id, name, slots, poles, frequency, phases, windingType.name, coilPitch, createdAt
)
