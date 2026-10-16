package com.windingmaster.presentation.checklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.windingmaster.R
import com.windingmaster.domain.model.ChecklistItem

@Composable
fun ChecklistScreen(viewModel: ChecklistViewModel = hiltViewModel()) {
    val list by viewModel.items.collectAsStateWithLifecycle()
    var newText by rememberSaveable { mutableStateOf("") }
    val done = list.count { it.isDone }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.checklist_progress_fmt, done, list.size),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::resetAll) { Text(stringResource(R.string.action_reset)) }
            }
            LinearProgressIndicator(
                progress = { if (list.isEmpty()) 0f else done / list.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(Modifier.weight(1f)) {
            items(list, key = { it.id }) { item ->
                ChecklistRow(item, onToggle = { viewModel.toggle(item) }, onDelete = { viewModel.delete(item.id) })
            }
        }

        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                placeholder = { Text(stringResource(R.string.checklist_add_hint)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.addCustom(newText); newText = "" }, enabled = newText.isNotBlank()) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        }
    }
}

@Composable
private fun ChecklistRow(item: ChecklistItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val text = item.customText
        ?: item.templateKey?.let { checklistStringRes[it] }?.let { stringResource(it) }
        ?: ""
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.isDone, onCheckedChange = { onToggle() })
        Text(
            text,
            modifier = Modifier.weight(1f),
            textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
        )
        if (item.customText != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}
