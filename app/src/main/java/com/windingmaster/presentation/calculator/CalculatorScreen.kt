package com.windingmaster.presentation.calculator

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R
import com.windingmaster.domain.model.SavedCalculation
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.domain.model.WindingType
import com.windingmaster.presentation.components.ChipRow
import com.windingmaster.presentation.components.DropdownField
import com.windingmaster.presentation.components.fmt
import com.windingmaster.presentation.components.labelRes
import com.windingmaster.presentation.components.messageRes
import com.windingmaster.presentation.components.rememberPhaseStyle
import com.windingmaster.presentation.diagram.ExtendedDiagram
import com.windingmaster.presentation.diagram.InteractiveDiagram
import com.windingmaster.presentation.diagram.WindingDiagram

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saved by viewModel.savedCalculations.collectAsStateWithLifecycle()
    var diagramIndex by rememberSaveable { mutableIntStateOf(2) }
    var showSave by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(state.lastSavedName) {
        state.lastSavedName?.let { name ->
            Toast.makeText(context, context.getString(R.string.calc_saved_msg, name), Toast.LENGTH_SHORT).show()
            viewModel.consumeSaved()
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InputsCard(state, viewModel)

        state.error?.let {
            Text(stringResource(it.messageRes()), color = MaterialTheme.colorScheme.error)
        }

        val result = state.result
        if (result != null) {
            result.warnings.forEach { w ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(stringResource(w.messageRes()), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            ResultCard(result)

            Text(stringResource(R.string.calc_diagram), style = MaterialTheme.typography.titleMedium)
            ChipRow(
                options = listOf(0, 1, 2),
                selected = diagramIndex,
                optionText = {
                    stringResource(
                        when (it) {
                            0 -> R.string.diagram_circular
                            1 -> R.string.diagram_extended
                            else -> R.string.diagram_interactive
                        }
                    )
                },
                onSelect = { diagramIndex = it }
            )
            when (diagramIndex) {
                0 -> WindingDiagram(result)
                1 -> ExtendedDiagram(result)
                else -> InteractiveDiagram(result, state.selectedSlot, viewModel::onSlotSelected)
            }

            Button(onClick = { showSave = true }) { Text(stringResource(R.string.calc_save)) }
        }

        SavedList(saved, onLoad = viewModel::loadSaved, onDelete = viewModel::deleteSaved)
    }

    if (showSave) {
        SaveDialog(
            onSave = { name -> viewModel.save(name); showSave = false },
            onDismiss = { showSave = false }
        )
    }
}

@Composable
private fun InputsCard(state: CalculatorUiState, vm: CalculatorViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.slots,
                    onValueChange = vm::onSlotsChange,
                    label = { Text(stringResource(R.string.calc_slots)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                DropdownField(
                    label = stringResource(R.string.calc_poles),
                    options = listOf(2, 4, 6, 8, 10, 12),
                    selected = state.poles,
                    optionText = { it.toString() },
                    onSelect = vm::onPolesChange,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = state.frequency,
                onValueChange = vm::onFrequencyChange,
                label = { Text(stringResource(R.string.calc_frequency)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.calc_phases), style = MaterialTheme.typography.labelLarge)
            ChipRow(
                options = listOf(1, 3),
                selected = state.phases,
                optionText = {
                    stringResource(if (it == 1) R.string.calc_phase_single else R.string.calc_phase_three)
                },
                onSelect = vm::onPhasesChange
            )
            DropdownField(
                label = stringResource(R.string.calc_winding_type),
                options = listOf(WindingType.FULL, WindingType.SHORTENED, WindingType.CONCENTRIC, WindingType.SERIES),
                selected = state.windingType,
                optionText = { stringResource(it.labelRes()) },
                onSelect = vm::onWindingTypeChange,
                modifier = Modifier.fillMaxWidth()
            )
            val clearIcon: (@Composable () -> Unit)? = if (state.customPitch.isNotEmpty()) {
                {
                    IconButton(onClick = vm::onResetPitch) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_reset))
                    }
                }
            } else null
            OutlinedTextField(
                value = state.customPitch,
                onValueChange = vm::onPitchChange,
                label = { Text(stringResource(R.string.calc_pitch)) },
                placeholder = { Text(state.result?.windingStep?.toString().orEmpty()) },
                supportingText = { Text(stringResource(R.string.calc_pitch_hint)) },
                trailingIcon = clearIcon,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ResultCard(r: WindingResult) {
    val style = rememberPhaseStyle(r.phases)
    val rows = listOf(
        stringResource(R.string.res_pole_pairs) to r.polePairs.toString(),
        stringResource(R.string.res_aez) to "${fmt(r.electricalAngle)}°",
        stringResource(R.string.res_q) to fmt(r.slotsPerPolePerPhase),
        stringResource(R.string.res_t) to fmt(r.polePitch),
        stringResource(R.string.res_n) to stringResource(R.string.calc_rpm_fmt, r.synchronousSpeed),
        stringResource(R.string.res_g) to r.windingStep.toString(),
        stringResource(R.string.res_g_reduced) to r.windingStepReduced.toString(),
        stringResource(R.string.res_y1) to r.coilPitchY1.toString(),
        stringResource(R.string.res_y2) to r.coilPitchY2.toString(),
        stringResource(R.string.res_b) to r.coilJumpB.toString(),
        stringResource(R.string.res_starts) to
            r.phaseStarts.mapIndexed { i, s -> "${style.label(i + 1)}1=$s" }.joinToString("   "),
        stringResource(R.string.res_fractional) to
            stringResource(if (r.isFractional) R.string.label_yes else R.string.label_no)
    )
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.calc_results), style = MaterialTheme.typography.titleMedium)
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.weight(1f))
                    Text(value, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SavedList(
    list: List<SavedCalculation>,
    onLoad: (SavedCalculation) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (list.isEmpty()) return
    Text(stringResource(R.string.calc_saved_list), style = MaterialTheme.typography.titleMedium)
    list.forEach { c ->
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(c.name, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.calc_saved_summary, c.slots, c.poles, c.phases),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = { onLoad(c) }) { Text(stringResource(R.string.action_load)) }
                IconButton(onClick = { onDelete(c.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@Composable
private fun SaveDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calc_save)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.calc_save_name)) },
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
