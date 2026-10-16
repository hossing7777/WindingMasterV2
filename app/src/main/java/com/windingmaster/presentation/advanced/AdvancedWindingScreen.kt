package com.windingmaster.presentation.advanced

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.FilterChip
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
import com.windingmaster.domain.model.PitchMode
import com.windingmaster.domain.model.SavedCalculation
import com.windingmaster.domain.model.WindingLayout
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.presentation.components.ChipRow
import com.windingmaster.presentation.components.DropdownField
import com.windingmaster.presentation.components.fmt
import com.windingmaster.presentation.components.messageRes
import com.windingmaster.presentation.components.rememberPhaseStyle
import com.windingmaster.presentation.diagram.ExtendedDiagram
import com.windingmaster.presentation.diagram.InteractiveDiagram
import com.windingmaster.presentation.diagram.WindingDiagram

@Composable
fun AdvancedWindingScreen(viewModel: AdvancedWindingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saved by viewModel.savedCalculations.collectAsStateWithLifecycle()
    var diagramIndex by rememberSaveable { mutableIntStateOf(1) }
    var showSave by rememberSaveable { mutableStateOf(false) }
    var showMain by rememberSaveable { mutableStateOf(true) }
    var showAux by rememberSaveable { mutableStateOf(true) }
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
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(
                "محاسبه‌گر پیشرفته — گام + چیدمان جداگانه با جدول کامل کویل‌ها",
                Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        AdvancedInputsCard(state, viewModel)

        state.error?.let {
            Text(stringResource(it.messageRes()), color = MaterialTheme.colorScheme.error)
        }

        val result = state.result
        if (result != null) {
            result.warnings.forEach { w ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(stringResource(w.messageRes()), Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            AdvancedResultCard(result)
            PitchTableCard(result)

            Text("نوع دیاگرام", style = MaterialTheme.typography.titleMedium)
            if (result.phases == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = showMain, onClick = { showMain = !showMain }, label = { Text("سیم اصلی") })
                    FilterChip(selected = showAux, onClick = { showAux = !showAux }, label = { Text("سیم فرعی") })
                }
            }
            ChipRow(
                options = listOf(0, 1, 2),
                selected = diagramIndex,
                optionText = { stringResource(when (it) { 0 -> R.string.diagram_circular; 1 -> R.string.diagram_extended; else -> R.string.diagram_interactive }) },
                onSelect = { diagramIndex = it }
            )
            when (diagramIndex) {
                0 -> WindingDiagram(result, selectedSlot = state.selectedSlot, onSlotTap = viewModel::onSlotSelected, showMain = showMain, showAux = showAux)
                1 -> ExtendedDiagram(result, selectedSlot = state.selectedSlot, onSlotClick = viewModel::onSlotSelected, showMain = showMain, showAux = showAux)
                else -> InteractiveDiagram(result, state.selectedSlot, viewModel::onSlotSelected)
            }

            Button(onClick = { showSave = true }) { Text(stringResource(R.string.calc_save)) }
        }

        SavedList(saved, onLoad = { /* placeholder */ }, onDelete = viewModel::deleteSaved)
    }

    if (showSave) {
        SaveDialog(
            onSave = { name -> viewModel.save(name); showSave = false },
            onDismiss = { showSave = false }
        )
    }
}

@Composable
private fun AdvancedInputsCard(state: AdvancedUiState, vm: AdvancedWindingViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.slots, onValueChange = vm::onSlotsChange,
                    label = { Text(stringResource(R.string.calc_slots)) }, singleLine = true,
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
                value = state.frequency, onValueChange = vm::onFrequencyChange,
                label = { Text(stringResource(R.string.calc_frequency)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.calc_phases), style = MaterialTheme.typography.labelLarge)
            ChipRow(
                options = listOf(1, 3), selected = state.phases,
                optionText = { stringResource(if (it == 1) R.string.calc_phase_single else R.string.calc_phase_three) },
                onSelect = vm::onPhasesChange
            )
            DropdownField(
                label = "گام سیم‌پیچی",
                options = PitchMode.values().toList(),
                selected = state.pitchMode,
                optionText = { if (it == PitchMode.FULL) "گام کامل (سیم بیشتر)" else "گام کسری (سیم کمتر)" },
                onSelect = vm::onPitchModeChange,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField(
                label = "چیدمان کویل‌ها",
                options = WindingLayout.values().toList(),
                selected = state.layout,
                optionText = { if (it == WindingLayout.CONCENTRIC) "متحدالمرکز (تودرتو)" else "سری (زنجیره‌ای)" },
                onSelect = vm::onLayoutChange,
                modifier = Modifier.fillMaxWidth()
            )
            val clearIcon: (@Composable () -> Unit)? = if (state.customPitch.isNotEmpty()) {
                { IconButton(onClick = vm::onResetPitch) { Icon(Icons.Default.Close, contentDescription = null) } }
            } else null
            OutlinedTextField(
                value = state.customPitch, onValueChange = vm::onPitchChange,
                label = { Text(stringResource(R.string.calc_pitch)) },
                placeholder = { Text(state.result?.coilPitchY1?.toString().orEmpty()) },
                supportingText = { Text("خالی = خودکار") },
                trailingIcon = clearIcon, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AdvancedResultCard(r: WindingResult) {
    val style = rememberPhaseStyle(r.phases)
    val rows = listOf(
        "جفت قطب (P)" to r.polePairs.toString(),
        "AEZ" to "${fmt(r.electricalAngle)}°",
        "q" to fmt(r.slotsPerPolePerPhase),
        "T" to fmt(r.polePitch),
        "N" to stringResource(R.string.calc_rpm_fmt, r.synchronousSpeed),
        "g" to r.windingStep.toString(),
        "g'" to r.windingStepReduced.toString(),
        "Y1" to r.coilPitchY1.toString(),
        "B (پرش)" to r.coilJumpB.toString(),
        "شروع فازها" to r.phaseStarts.mapIndexed { i, s -> "${style.label(i + 1)}1=$s" }.joinToString("   "),
        "سیم‌پیچی کسری" to stringResource(if (r.isFractional) R.string.label_yes else R.string.label_no)
    )
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.calc_results), style = MaterialTheme.typography.titleMedium)
            rows.forEachIndexed { i, (k, v) ->
                if (i > 0) HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(k, Modifier.weight(1f))
                    Text(v, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PitchTableCard(r: WindingResult) {
    if (r.pitchList.isEmpty()) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("جدول گام کویل‌ها", style = MaterialTheme.typography.titleMedium)
            Text(
                "نوع: " + (if (r.pitchMode == PitchMode.FULL) "گام کامل" else "گام کسری") +
                "  |  چیدمان: " + (if (r.layout == WindingLayout.CONCENTRIC) "متحدالمرکز" else "سری"),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            r.pitchList.forEachIndexed { idx, pitch ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("کویل ${idx + 1}", Modifier.weight(1f))
                    Text("y${idx + 1} = $pitch", fontWeight = FontWeight.Bold)
                }
                if (idx < r.pitchList.size - 1) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SavedList(list: List<SavedCalculation>, onLoad: (SavedCalculation) -> Unit, onDelete: (Long) -> Unit) {
    if (list.isEmpty()) return
    Text(stringResource(R.string.calc_saved_list), style = MaterialTheme.typography.titleMedium)
    list.forEach { c ->
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(c.name, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.calc_saved_summary, c.slots, c.poles, c.phases), style = MaterialTheme.typography.bodySmall)
                }
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
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("نام") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}
