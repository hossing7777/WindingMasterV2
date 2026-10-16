package com.windingmaster.presentation.notes

import android.content.Intent
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R

@Composable
fun NoteEditScreen(onDone: () -> Unit, viewModel: NoteEditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val note = state.note
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = note.title,
            onValueChange = viewModel::onTitle,
            label = { Text(stringResource(R.string.note_title)) },
            isError = state.titleMissing,
            supportingText = if (state.titleMissing) {
                { Text(stringResource(R.string.note_title_required)) }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = note.body,
            onValueChange = viewModel::onBody,
            label = { Text(stringResource(R.string.note_body)) },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_save))
            }
            OutlinedButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, note.title)
                        putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.body}")
                    }
                    context.startActivity(Intent.createChooser(send, null))
                },
                enabled = note.title.isNotBlank() || note.body.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.action_share)) }
        }
    }
}
