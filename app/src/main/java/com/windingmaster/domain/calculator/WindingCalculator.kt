package com.windingmaster.domain.calculator

import com.windingmaster.domain.model.CalcWarning
import com.windingmaster.domain.model.InvalidWindingInputException
import com.windingmaster.domain.model.PitchMode
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingInput
import com.windingmaster.domain.model.WindingLayout
import com.windingmaster.domain.model.WindingResult
import javax.inject.Inject
import kotlin.math.ceil

class WindingCalculator @Inject constructor(
    private val assigner: CoilGroupAssigner
) {

    fun validate(input: WindingInput): ValidationError? {
        if (input.slots !in 12..200) return ValidationError.SLOTS_OUT_OF_RANGE
        if (input.slots % 2 != 0) return ValidationError.SLOTS_ODD
        if (input.poles !in 2..12 || input.poles % 2 != 0) return ValidationError.POLES_INVALID
        if (input.frequency !in 1..400) return ValidationError.FREQUENCY_INVALID
        if (input.phases != 1 && input.phases != 3) return ValidationError.PHASES_INVALID
        val pitch = input.coilPitch
        if (pitch != null && pitch !in 1..(input.slots / 2)) return ValidationError.PITCH_OUT_OF_RANGE
        return null
    }

    fun calculate(input: WindingInput): WindingResult {
        validate(input)?.let { throw InvalidWindingInputException(it) }

        val z = input.slots
        val poles = input.poles
        val pairs = poles / 2
        val windings = if (input.phases == 3) 3 else 2

        // فرمول‌های پایه
        val aez = 360.0 * pairs / z
        val q = z.toDouble() / (poles * windings)
        val t = z.toDouble() / poles
        val n = 60 * input.frequency / pairs

        // گام قطبی
        val g = z / poles
        val gReduced = (g - 2).coerceAtLeast(1)

        // تعداد کویل در هر گروه
        val coilsPerGroup = ceil(q - 1e-9).toInt().coerceAtLeast(1)

        // Y1 بر اساس گام
        val y1Auto: Int = when (input.pitchMode) {
            PitchMode.FULL -> (g + q.toInt()).coerceAtLeast(1)         // گام کامل: Z/2P + q
            PitchMode.FRACTIONAL -> {
                // گام کسری: B × (Z/2P) + q/2  با B = 1/2
                val half = (g * 0.5 + q / 2.0)
                ceil(half - 1e-9).toInt().coerceAtLeast(1)
            }
        }

        // لیست گام کویل‌ها
        val pitchList: List<Int> = when (input.layout) {
            WindingLayout.CONCENTRIC -> {
                (0 until coilsPerGroup).map { k -> (y1Auto - 2 * k).coerceAtLeast(1) }
            }
            WindingLayout.SERIES -> {
                val step = if (input.pitchMode == PitchMode.FULL) g else gReduced
                List(coilsPerGroup) { step }
            }
        }

        val y1 = input.coilPitch ?: pitchList.first()
        val y2 = pitchList.getOrElse(1) { y1 }

        // شروع فازها
        fun startOf(deg: Int): Int = ceil(deg / aez - 1e-9).toInt() + 1
        val starts = if (input.phases == 3) listOf(1, startOf(120), startOf(240))
                     else listOf(1, startOf(180))

        val fractional = z % (poles * windings) != 0

        val warnings = buildList {
            if (fractional) add(CalcWarning.FRACTIONAL_WINDING)
            if (z % poles != 0) add(CalcWarning.FRACTIONAL_PITCH)
            if (input.phases == 3 && z % 3 != 0) add(CalcWarning.SLOTS_NOT_DIVISIBLE_BY_3)
        }

        // استفاده از windingType موجود برای سازگاری با CoilGroupAssigner
        val type = when (input.layout) {
            WindingLayout.CONCENTRIC -> com.windingmaster.domain.model.WindingType.FULL_CONCENTRIC
            WindingLayout.SERIES -> com.windingmaster.domain.model.WindingType.FULL_SERIES
        }

        val out = assigner.assign(z, pairs, input.phases, type, y1, y2)

        val b = ceil(t * 0.5 + 1.0 - 1e-9).toInt()

        return WindingResult(
            z = z, poles = poles, polePairs = pairs, phases = input.phases,
            frequency = input.frequency,
            isFractional = fractional,
            isConcentric = input.layout == WindingLayout.CONCENTRIC,
            isShortPitched = input.pitchMode == PitchMode.FRACTIONAL,
            electricalAngle = aez,
            slotsPerPolePerPhase = q,
            polePitch = t,
            synchronousSpeed = n,
            phaseStarts = starts,
            windingStep = g,
            windingStepReduced = gReduced,
            coilPitchY1 = y1,
            coilPitchY2 = y2,
            coilJumpB = b,
            pitchList = pitchList,
            pitchMode = input.pitchMode,
            layout = input.layout,
            slotAssignments = out.assignments,
            coilGroups = out.groups,
            warnings = warnings
        )
    }

    private companion object {
        const val EPS = 1e-9
    }
}
