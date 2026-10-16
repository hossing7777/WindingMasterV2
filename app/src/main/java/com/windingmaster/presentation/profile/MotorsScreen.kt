package com.windingmaster.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.presentation.components.ConfirmDeleteDialog
import com.windingmaster.presentation.components.fmt

private fun motorSummary(m: MotorProfile): String = listOfNotNull(
    m.manufacturer.takeIf { it.isNotBlank() },
    m.powerKw?.let { "${fmt(it)} kW" },
    m.slots?.let { "Z=$it" },
    m.poles?.let { "2p=$it" }
).joinToString(" • ")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MotorsScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MotorListViewModel = hiltViewModel()
) {
    val motors by viewModel.motors.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<MotorProfile?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (motors.isEmpty()) {
            Text(
                stringResource(R.string.motors_empty),
                Modifier.align(Alignment.Center).padding(24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Text(stringResource(R.string.motors_hint), style = MaterialTheme.typography.labelSmall) }
                items(motors, key = { it.id }) { m ->
                    Card(
                        Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEdit(m.id) },
                            onLongClick = { toDelete = m }
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val summary = motorSummary(m)
                            if (summary.isNotEmpty()) Text(summary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
        }
    }

    toDelete?.let { m ->
        ConfirmDeleteDialog(
            name = m.name,
            onConfirm = { viewModel.delete(m.id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}
