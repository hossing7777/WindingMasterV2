package com.windingmaster.domain.calculator

import com.windingmaster.domain.model.CoilGroup
import com.windingmaster.domain.model.PhaseCode
import com.windingmaster.domain.model.SlotAssignment
import com.windingmaster.domain.model.WindingType
import javax.inject.Inject

/**
 * تخصیص فاز/جهت به شیارها با حساب صحیح (بدون خطای اعشاری).
 *
 * سه‌فاز: هر دور الکتریکی 6 کمربند 60 درجه‌ای دارد:
 *   U+ , W- , V+ , U- , W+ , V-
 * کمربند شیار i برابر است با ((i-1) * 6 * P) / Z (تقسیم صحیح).
 * وقتی q کسری باشد، اندازه‌ی کمربندها خودبه‌خود 3-2-3-2 می‌شود.
 *
 * تک‌فاز: کمربندهای 180 درجه‌ای، زوج = Main (بالا)، فرد = Aux (پایین).
 *
 * متحدالمرکز (Concentric):
 *   کویل‌ها در هر کمربند به صورت قرینه تودرتو می‌شوند:
 *   شیار اول کمربند ←→ شیار آخر کمربند (بزرگ‌ترین گام)
 *   شیار دوم ←→ شیار یکی‌مانده‌به‌آخر (گام کوچک‌تر)
 *   ...
 *   نیمه‌ی دوم کمربند، جفت‌شدن معکوس دارند (به شیار شروع برمی‌گردند).
 */
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

        val isConcentric = type == WindingType.CONCENTRIC

        // محاسبه‌ی لیست گام‌ها برای هر شیار در یک کمربند با n شیار
        fun pitchesFor(n: Int): List<Int> = when {
            isConcentric -> {
                // الگوی قرینه: n-1, n-3, n-5, ..., 1, 1, ..., n-5, n-3, n-1
                List(n) { k ->
                    val distFromEdge = minOf(k, n - 1 - k)
                    (n - 1 - 2 * distFromEdge).coerceAtLeast(1)
                }
            }
            type == WindingType.SERIES -> List(n) { y2 }
            else -> List(n) { y2 }
        }

        val groupType = when {
            fractional -> "FRACTIONAL"
            isConcentric -> "CONCENTRIC"
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
            val n = slots.size
            val pitches = pitchesFor(n)
            groupsOfPhase.add(slots)
            pitchesOfPhase.add(pitches)

            val midPoint = (n + 1) / 2  // مرز نیمه‌ی اول و دوم
            slots.forEachIndexed { k, slot ->
                val pitch = pitches[k]
                // برای متحدالمرکز: نیمه‌ی دوم به عقب جفت می‌شود
                val isSecondHalf = isConcentric && k >= midPoint
                val pairedSlot = if (isSecondHalf) {
                    ((slot - 1 - pitch) % z + z) % z + 1
                } else {
                    (slot - 1 + pitch) % z + 1
                }
                assignments.add(
                    SlotAssignment(
                        slotNumber = slot,
                        phase = phase,
                        direction = dir,
                        groupIndex = groupIndex,
                        pairedSlot = pairedSlot
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
