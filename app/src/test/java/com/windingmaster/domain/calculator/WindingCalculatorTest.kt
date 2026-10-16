package com.windingmaster.domain.calculator

import com.windingmaster.domain.model.InvalidWindingInputException
import com.windingmaster.domain.model.PhaseCode
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class WindingCalculatorTest {

    private val calculator = WindingCalculator(CoilGroupAssigner())
    private val delta = 1e-9

    @Test
    fun z24_p4_threePhase() {
        val r = calculator.calculate(WindingInput(slots = 24, poles = 4))
        assertEquals(30.0, r.electricalAngle, delta)
        assertEquals(2.0, r.slotsPerPolePerPhase, delta)
        assertEquals(6.0, r.polePitch, delta)
        assertEquals(6, r.windingStep)
        assertEquals(1500, r.synchronousSpeed)
        assertEquals(listOf(1, 5, 9), r.phaseStarts)
        assertFalse(r.isFractional)
        assertEquals(4, r.coilJumpB)
    }

    @Test
    fun z36_p4_threePhase() {
        val r = calculator.calculate(WindingInput(slots = 36, poles = 4))
        assertEquals(20.0, r.electricalAngle, delta)
        assertEquals(3.0, r.slotsPerPolePerPhase, delta)
        assertEquals(9.0, r.polePitch, delta)
        assertEquals(9, r.windingStep)
        assertEquals(1500, r.synchronousSpeed)
    }

    @Test
    fun z30_p4_fractional() {
        val r = calculator.calculate(WindingInput(slots = 30, poles = 4))
        assertEquals(2.5, r.slotsPerPolePerPhase, delta)
        assertTrue(r.isFractional)
        assertEquals(listOf(3, 2, 3, 2), r.coilGroups[0].slots.map { it.size })
        assertEquals(10, r.coilGroups[0].slots.flatten().size)
    }

    @Test
    fun everyPhaseGetsEqualSlots_andSlotsAreCoveredOnce() {
        val r = calculator.calculate(WindingInput(slots = 24, poles = 4))
        val perPhase = r.slotAssignments.groupBy { it.phase }.mapValues { it.value.size }
        assertEquals(mapOf(PhaseCode.U to 8, PhaseCode.V to 8, PhaseCode.W to 8), perPhase)
        assertEquals((1..24).toList(), r.slotAssignments.map { it.slotNumber })
    }

    @Test
    fun invalidInputsAreRejected() {
        assertEquals(ValidationError.SLOTS_OUT_OF_RANGE, calculator.validate(WindingInput(10, 4)))
        assertEquals(ValidationError.SLOTS_ODD, calculator.validate(WindingInput(25, 4)))
        assertEquals(ValidationError.POLES_INVALID, calculator.validate(WindingInput(24, 14)))
        try {
            calculator.calculate(WindingInput(24, 3))
            fail("expected InvalidWindingInputException")
        } catch (e: InvalidWindingInputException) {
            assertEquals(ValidationError.POLES_INVALID, e.error)
        }
    }
}
