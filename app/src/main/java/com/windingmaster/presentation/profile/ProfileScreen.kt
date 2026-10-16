package com.windingmaster.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.model.Note
import com.windingmaster.presentation.components.fmt
import com.windingmaster.presentation.components.labelRes

@Composable
fun ProfileScreen(
    onSelectMotor: (Long) -> Unit,
    onEditMotor: (Long) -> Unit,
    onOpenChecklist: (Long) -> Unit,
    onNewNote: (Long) -> Unit,
    onOpenNote: (noteId: Long, motorId: Long) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val motors by viewModel.motors.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val list = motors
    val selected = list?.firstOrNull { it.id == viewModel.motorId }

    when {
        list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        selected == null -> MotorPicker(list, onSelectMotor)
        else -> MotorDetails(selected, notes, onEditMotor, onOpenChecklist, onNewNote, onOpenNote)
    }
}

@Composable
private fun MotorPicker(motors: List<MotorProfile>, onSelect: (Long) -> Unit) {
    if (motors.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.profile_no_motors), Modifier.padding(24.dp), textAlign = TextAlign.Center)
        }
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(R.string.profile_select_motor), style = MaterialTheme.typography.bodyMedium)
        motors.forEach { m ->
            Card(Modifier.fillMaxWidth().clickable { onSelect(m.id) }) {
                Text(m.name, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun MotorDetails(
    m: MotorProfile,
    notes: List<Note>,
    onEdit: (Long) -> Unit,
    onOpenChecklist: (Long) -> Unit,
    onNewNote: (Long) -> Unit,
    onOpenNote: (Long, Long) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(m.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                InfoRow(R.string.field_manufacturer, m.manufacturer.takeIf { it.isNotBlank() })
                InfoRow(R.string.field_power_kw, m.powerKw?.let { fmt(it) })
                InfoRow(R.string.field_voltage, m.voltage?.toString())
                InfoRow(R.string.field_current, m.currentA?.let { fmt(it) })
                InfoRow(R.string.field_rpm, m.rpm?.toString())
                InfoRow(R.string.field_phases, m.phases.toString())
                InfoRow(R.string.field_slots, m.slots?.toString())
                InfoRow(R.string.field_poles, m.poles?.toString())
                InfoRow(R.string.field_connection, stringResource(m.connection.labelRes()))
                InfoRow(R.string.field_wire_diameter, m.wireDiameterMm?.let { fmt(it) })
                InfoRow(R.string.field_turns_per_coil, m.turnsPerCoil?.toString())
                InfoRow(R.string.field_notes, m.notes.takeIf { it.isNotBlank() })
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEdit(m.id) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_edit))
            }
            Button(onClick = { onOpenChecklist(m.id) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.profile_open_checklist), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.profile_notes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { onNewNote(m.id) }) { Text(stringResource(R.string.profile_new_note)) }
        }
        if (notes.isEmpty()) {
            Text(stringResource(R.string.profile_notes_empty), style = MaterialTheme.typography.bodySmall)
        } else {
            notes.forEach { n ->
                Card(Modifier.fillMaxWidth().clickable { onOpenNote(n.id, m.id) }) {
                    Text(n.title, Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(labelRes: Int, value: String?) {
    if (value == null) return
    Row(Modifier.fillMaxWidth()) {
        Text(stringResource(labelRes), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
