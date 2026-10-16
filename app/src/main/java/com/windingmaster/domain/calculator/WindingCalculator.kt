package com.windingmaster.domain.calculator

import com.windingmaster.domain.model.CalcWarning
import com.windingmaster.domain.model.InvalidWindingInputException
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingInput
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.domain.model.WindingType
import javax.inject.Inject

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

        val aez = 360.0 * pairs / z
        val q = z.toDouble() / (poles * windings)
        val t = z.toDouble() / poles
        val n = 60 * input.frequency / pairs

        // گام قطبی به عدد صحیح (floor)
        val g = z / poles
        val gReduced = (g - 2).coerceAtLeast(1)

        // محاسبه‌ی y1 (گام بیرونی) و y2 (گام داخلی/یکنواخت) بر اساس نوع
        val (y1Auto, y2Auto) = when (input.windingType) {
            WindingType.FULL_CONCENTRIC -> (g - 1).coerceAtLeast(1) to (g - 1).coerceAtLeast(1)
            WindingType.FULL_SERIES -> g to g
            WindingType.FRACTIONAL_CONCENTRIC -> (g - 2).coerceAtLeast(1) to (g - 2).coerceAtLeast(1)
            WindingType.FRACTIONAL_SERIES -> (g - 2).coerceAtLeast(1) to (g - 2).coerceAtLeast(1)
        }

        val y1 = input.coilPitch ?: y1Auto
        val y2 = input.coilPitch ?: y2Auto

        // شروع فازها
        fun startOf(deg: Int): Int = kotlin.math.ceil(deg / aez - EPS).toInt() + 1
        val starts = if (input.phases == 3) listOf(1, startOf(120), startOf(240))
                     else listOf(1, startOf(180))

        val fractional = z % (poles * windings) != 0

        val warnings = buildList {
            if (fractional) add(CalcWarning.FRACTIONAL_WINDING)
            if (z % poles != 0) add(CalcWarning.FRACTIONAL_PITCH)
            if (input.phases == 3 && z % 3 != 0) add(CalcWarning.SLOTS_NOT_DIVISIBLE_BY_3)
        }

        val out = assigner.assign(z, pairs, input.phases, input.windingType, y1, y2)

        // B = ceil(T/2 + 1) — شیار پرش برای کنسنتریک
        val b = kotlin.math.ceil(t * 0.5 + 1.0 - EPS).toInt()

        return WindingResult(
            z = z, poles = poles, polePairs = pairs, phases = input.phases,
            frequency = input.frequency,
            isFractional = fractional,
            isConcentric = input.windingType.isConcentric,
            isShortPitched = input.windingType.isFractional,
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
            slotAssignments = out.assignments,
            coilGroups = out.groups,
            warnings = warnings
        )
    }

    private companion object {
        const val EPS = 1e-9
    }
}
