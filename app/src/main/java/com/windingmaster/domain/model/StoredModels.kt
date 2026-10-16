package com.windingmaster.domain.model

enum class ConnectionType { STAR, DELTA }

enum class WindingType { FULL, SHORTENED, CONCENTRIC, SERIES }

data class MotorProfile(
    val id: Long = 0,
    val name: String,
    val manufacturer: String = "",
    val powerKw: Double? = null,
    val voltage: Int? = null,
    val currentA: Double? = null,
    val rpm: Int? = null,
    val phases: Int = 3,
    val slots: Int? = null,
    val poles: Int? = null,
    val connection: ConnectionType = ConnectionType.STAR,
    val wireDiameterMm: Double? = null,
    val turnsPerCoil: Int? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChecklistItem(
    val id: Long = 0,
    val motorId: Long? = null,
    val templateKey: String? = null,
    val customText: String? = null,
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)

data class Note(
    val id: Long = 0,
    val motorId: Long? = null,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavedCalculation(
    val id: Long = 0,
    val name: String,
    val slots: Int,
    val poles: Int,
    val frequency: Int,
    val phases: Int,
    val windingType: WindingType,
    val coilPitch: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
