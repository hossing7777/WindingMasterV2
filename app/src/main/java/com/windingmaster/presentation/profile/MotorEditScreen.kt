package com.windingmaster.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R
import com.windingmaster.domain.model.ConnectionType
import com.windingmaster.presentation.components.ChipRow
import com.windingmaster.presentation.components.DecimalField
import com.windingmaster.presentation.components.IntField
import com.windingmaster.presentation.components.labelRes

@Composable
fun MotorEditScreen(onDone: () -> Unit, viewModel: MotorEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val m = state.motor
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = m.name,
            onValueChange = { v -> viewModel.edit { it.copy(name = v) } },
            label = { Text(stringResource(R.string.field_name)) },
            isError = state.nameMissing,
            supportingText = if (state.nameMissing) {
                { Text(stringResource(R.string.error_name_required)) }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = m.manufacturer,
            onValueChange = { v -> viewModel.edit { it.copy(manufacturer = v) } },
            label = { Text(stringResource(R.string.field_manufacturer)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        DecimalField(
            label = stringResource(R.string.field_power_kw),
            value = m.powerKw,
            onValue = { v -> viewModel.edit { it.copy(powerKw = v) } }
        )
        IntField(
            label = stringResource(R.string.field_voltage),
            value = m.voltage,
            onValue = { v -> viewModel.edit { it.copy(voltage = v) } }
        )
        DecimalField(
            label = stringResource(R.string.field_current),
            value = m.currentA,
            onValue = { v -> viewModel.edit { it.copy(currentA = v) } }
        )
        IntField(
            label = stringResource(R.string.field_rpm),
            value = m.rpm,
            onValue = { v -> viewModel.edit { it.copy(rpm = v) } }
        )

        Text(stringResource(R.string.field_phases), style = MaterialTheme.typography.labelLarge)
        ChipRow(
            options = listOf(1, 3),
            selected = m.phases,
            optionText = {
                stringResource(if (it == 1) R.string.calc_phase_single else R.string.calc_phase_three)
            },
            onSelect = { p -> viewModel.edit { it.copy(phases = p) } }
        )

        IntField(
            label = stringResource(R.string.field_slots),
            value = m.slots,
            onValue = { v -> viewModel.edit { it.copy(slots = v) } }
        )
        IntField(
            label = stringResource(R.string.field_poles),
            value = m.poles,
            onValue = { v -> viewModel.edit { it.copy(poles = v) } }
        )

        Text(stringResource(R.string.field_connection), style = MaterialTheme.typography.labelLarge)
        ChipRow(
            options = ConnectionType.values().toList(),
            selected = m.connection,
            optionText = { stringResource(it.labelRes()) },
            onSelect = { c -> viewModel.edit { it.copy(connection = c) } }
        )

        DecimalField(
            label = stringResource(R.string.field_wire_diameter),
            value = m.wireDiameterMm,
            onValue = { v -> viewModel.edit { it.copy(wireDiameterMm = v) } }
        )
        IntField(
            label = stringResource(R.string.field_turns_per_coil),
            value = m.turnsPerCoil,
            onValue = { v -> viewModel.edit { it.copy(turnsPerCoil = v) } }
        )

        OutlinedTextField(
            value = m.notes,
            onValueChange = { v -> viewModel.edit { it.copy(notes = v) } },
            label = { Text(stringResource(R.string.field_notes)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save))
        }
    }
}
