package com.windingmaster.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.model.Note
import com.windingmaster.domain.repository.MotorRepository
import com.windingmaster.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    motorRepository: MotorRepository,
    noteRepository: NoteRepository
) : ViewModel() {

    val motorId: Long? = savedStateHandle.get<Long>("motorId")?.takeIf { it >= 0 }

    val motors: StateFlow<List<MotorProfile>?> =
        motorRepository.observeAll()
            .map<List<MotorProfile>, List<MotorProfile>?> { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notes: StateFlow<List<Note>> =
        noteRepository.observeAll()
            .map { all -> all.filter { it.motorId != null && it.motorId == motorId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
