package com.windingmaster.presentation.diagram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.windingmaster.R
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.presentation.components.DiagramLegend
import com.windingmaster.presentation.components.rememberPhaseStyle

@Composable
fun InteractiveDiagram(
    result: WindingResult,
    selectedSlot: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = rememberPhaseStyle(result.phases)
    val history = remember(result.z, result.poles, result.phases) { mutableStateListOf<Int>() }

    val pick: (Int) -> Unit = { slot ->
        if (slot != selectedSlot) {
            selectedSlot?.let { history.add(it) }
            onSelect(slot)
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.interactive_hint), style = MaterialTheme.typography.bodySmall)
        WindingDiagram(result, selectedSlot = selectedSlot, onSlotTap = pick, showLegend = false)
        ExtendedDiagram(result, selectedSlot = selectedSlot, onSlotClick = pick, showLegend = false)
        DiagramLegend(style)
        SlotInfoCard(result, selectedSlot)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { history.clear(); onSelect(null) },
                enabled = selectedSlot != null
            ) { Text(stringResource(R.string.action_reset_selection)) }
            OutlinedButton(
                onClick = { onSelect(history.removeLastOrNull()) },
                enabled = selectedSlot != null
            ) { Text(stringResource(R.string.action_undo)) }
        }
    }
}

@Composable
private fun SlotInfoCard(result: WindingResult, selectedSlot: Int?) {
    val style = rememberPhaseStyle(result.phases)
    val assignment = selectedSlot?.let { s -> result.slotAssignments.firstOrNull { it.slotNumber == s } }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (assignment == null) {
                Text(stringResource(R.string.slot_none_selected))
            } else {
                val span = ((assignment.pairedSlot - assignment.slotNumber) % result.z + result.z) % result.z
                Text(
                    stringResource(R.string.slot_title_fmt, assignment.slotNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(stringResource(R.string.slot_phase_fmt, style.label(assignment.phase)))
                Text(
                    stringResource(
                        R.string.slot_direction_fmt,
                        stringResource(if (assignment.direction == 1) R.string.dir_up else R.string.dir_down)
                    )
                )
                Text(stringResource(R.string.slot_group_fmt, assignment.groupIndex + 1))
                Text(stringResource(R.string.slot_paired_fmt, assignment.pairedSlot))
                Text(stringResource(R.string.slot_pitch_fmt, span))
            }
        }
    }
}
