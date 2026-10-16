package com.windingmaster.domain.model

object PhaseCode {
    const val NONE = 0
    const val U = 1
    const val V = 2
    const val W = 3
    const val MAIN = 1
    const val AUX = 2
}

/** گام سیم‌پیچی (فقط برای تب پیشرفته) */
enum class PitchMode { FULL, FRACTIONAL }

/** چیدمان کویل‌ها (فقط برای تب پیشرفته) */
enum class WindingLayout { CONCENTRIC, SERIES }

data class WindingInput(
    val slots: Int,
    val poles: Int,
    val frequency: Int = 50,
    val phases: Int = 3,
    val windingType: WindingType = WindingType.FULL_CONCENTRIC,
    /** برای تب پیشرفته — جایگزین windingType */
    val pitchMode: PitchMode = PitchMode.FULL,
    val layout: WindingLayout = WindingLayout.CONCENTRIC,
    val coilPitch: Int? = null
)

enum class ValidationError {
    SLOTS_OUT_OF_RANGE, SLOTS_ODD, POLES_INVALID,
    FREQUENCY_INVALID, PHASES_INVALID, PITCH_OUT_OF_RANGE
}

enum class CalcWarning {
    FRACTIONAL_WINDING, FRACTIONAL_PITCH, SLOTS_NOT_DIVISIBLE_BY_3
}

class InvalidWindingInputException(val error: ValidationError) :
    IllegalArgumentException(error.name)

data class SlotAssignment(
    val slotNumber: Int,
    val phase: Int,
    val direction: Int,
    val groupIndex: Int,
    val pairedSlot: Int
)

data class CoilGroup(
    val phase: Int,
    val slots: List<List<Int>>,
    val pitches: List<List<Int>>,
    val type: String
)

data class WindingResult(
    val z: Int,
    val poles: Int,
    val polePairs: Int,
    val phases: Int,
    val frequency: Int,
    val isFractional: Boolean,
    val isConcentric: Boolean,
    val isShortPitched: Boolean,
    val electricalAngle: Double,
    val slotsPerPolePerPhase: Double,
    val polePitch: Double,
    val synchronousSpeed: Int,
    val phaseStarts: List<Int>,
    val windingStep: Int,
    val windingStepReduced: Int,
    val coilPitchY1: Int,
    val coilPitchY2: Int,
    val coilJumpB: Int,
    /** لیست کامل گام کویل‌های یک گروه — برای جدول پیشرفته */
    val pitchList: List<Int> = emptyList(),
    /** حالت گام (فقط برای نمایش در تب پیشرفته) */
    val pitchMode: PitchMode = PitchMode.FULL,
    /** چیدمان (فقط برای نمایش در تب پیشرفته) */
    val layout: WindingLayout = WindingLayout.CONCENTRIC,
    val slotAssignments: List<SlotAssignment>,
    val coilGroups: List<CoilGroup>,
    val warnings: List<CalcWarning>
)
