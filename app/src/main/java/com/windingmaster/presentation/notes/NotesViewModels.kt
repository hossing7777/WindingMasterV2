package com.windingmaster.presentation.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windingmaster.domain.model.Note
import com.windingmaster.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    val notes: StateFlow<List<Note>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
}

data class NoteEditUiState(
    val note: Note = Note(title = "", body = ""),
    val titleMissing: Boolean = false,
    val loading: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditUiState())
    val state: StateFlow<NoteEditUiState> = _state.asStateFlow()

    init {
        val noteId = savedStateHandle.get<Long>("noteId")?.takeIf { it >= 0 }
        val motorId = savedStateHandle.get<Long>("motorId")?.takeIf { it >= 0 }
        viewModelScope.launch {
            val existing = noteId?.let { repository.getById(it) }
            _state.update {
                it.copy(note = existing ?: Note(motorId = motorId, title = "", body = ""), loading = false)
            }
        }
    }

    fun onTitle(v: String) = _state.update { it.copy(note = it.note.copy(title = v), titleMissing = false) }
    fun onBody(v: String) = _state.update { it.copy(note = it.note.copy(body = v)) }

    fun save() {
        val note = _state.value.note
        if (note.title.isBlank()) {
            _state.update { it.copy(titleMissing = true) }
            return
        }
        viewModelScope.launch {
            repository.save(note.copy(title = note.title.trim()))
            _state.update { it.copy(saved = true) }
        }
    }
}
