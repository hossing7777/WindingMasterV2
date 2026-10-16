package com.windingmaster.domain.calculator

import com.windingmaster.domain.model.CoilGroup
import com.windingmaster.domain.model.PhaseCode
import com.windingmaster.domain.model.SlotAssignment
import com.windingmaster.domain.model.WindingType
import javax.inject.Inject

class CoilGroupAssigner @Inject constructor() {

    data class Output(
        val assignments: List<SlotAssignment>,
        val groups: List<CoilGroup>
    )

    fun assign(
        z: Int,
        polePairs: Int,
        phases: Int,
        type: WindingType,
        fractional: Boolean,
        y1: Int,
        y2: Int
    ): Output {
        val beltsPerCycle = if (phases == 3) 6 else 2

        fun belt(slot: Int): Int =
            (((slot - 1).toLong() * beltsPerCycle * polePairs) / z).toInt()

        fun phaseAndDirection(b: Int): Pair<Int, Int> =
            if (phases == 3) when (b % 6) {
                0 -> PhaseCode.U to 1
                1 -> PhaseCode.W to -1
                2 -> PhaseCode.V to 1
                3 -> PhaseCode.U to -1
                4 -> PhaseCode.W to 1
                else -> PhaseCode.V to -1
            } else {
                if (b % 2 == 0) PhaseCode.MAIN to 1 else PhaseCode.AUX to -1
            }

        val beltSlots = LinkedHashMap<Int, MutableList<Int>>()
        for (s in 1..z) beltSlots.getOrPut(belt(s)) { mutableListOf() }.add(s)

        fun pitchesFor(n: Int): List<Int> = when (type) {
            WindingType.CONCENTRIC -> List(n) { k -> (y1 - 2 * k).coerceAtLeast(1) }
            else -> List(n) { y2 }
        }

        val groupType = when {
            fractional -> "FRACTIONAL"
            type == WindingType.CONCENTRIC -> "CONCENTRIC"
            type == WindingType.SERIES -> "SERIES"
            else -> "FULL"
        }

        val phaseSlots = HashMap<Int, MutableList<List<Int>>>()
        val phasePitches = HashMap<Int, MutableList<List<Int>>>()
        val assignments = ArrayList<SlotAssignment>(z)

        for ((b, slots) in beltSlots) {
            val (phase, dir) = phaseAndDirection(b)
            val groupsOfPhase = phaseSlots.getOrPut(phase) { mutableListOf() }
            val pitchesOfPhase = phasePitches.getOrPut(phase) { mutableListOf() }
            val groupIndex = groupsOfPhase.size
            val pitches = pitchesFor(slots.size)
            groupsOfPhase.add(slots)
            pitchesOfPhase.add(pitches)
            slots.forEachIndexed { k, slot ->
                assignments.add(
                    SlotAssignment(
                        slotNumber = slot,
                        phase = phase,
                        direction = dir,
                        groupIndex = groupIndex,
                        pairedSlot = (slot - 1 + pitches[k]) % z + 1
                    )
                )
            }
        }

        val groups = phaseSlots.keys.sorted().map { phase ->
            CoilGroup(
                phase = phase,
                slots = phaseSlots.getValue(phase),
                pitches = phasePitches.getValue(phase),
                type = groupType
            )
        }
        return Output(assignments.sortedBy { it.slotNumber }, groups)
    }
}
