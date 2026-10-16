package com.windingmaster.presentation.diagram

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    var showMain by rememberSaveable { mutableStateOf(true) }
    var showAux by rememberSaveable { mutableStateOf(true) }

    val pick: (Int) -> Unit = { slot ->
        if (slot != selectedSlot) {
            selectedSlot?.let { history.add(it) }
            onSelect(slot)
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.interactive_hint), style = MaterialTheme.typography.bodySmall)

        if (result.phases == 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showMain,
                    onClick = { showMain = !showMain },
                    label = { Text("سیم اصلی") }
                )
                FilterChip(
                    selected = showAux,
                    onClick = { showAux = !showAux },
                    label = { Text("سیم فرعی") }
                )
            }
        }

        WindingDiagram(
            result,
            selectedSlot = selectedSlot,
            onSlotTap = pick,
            showLegend = false,
            showMain = showMain,
            showAux = showAux
        )
        ExtendedDiagram(
            result,
            selectedSlot = selectedSlot,
            onSlotClick = pick,
            showLegend = false,
            showMain = showMain,
            showAux = showAux
        )
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
                val span = kotlin.math.abs(assignment.pairedSlot - assignment.slotNumber)
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
