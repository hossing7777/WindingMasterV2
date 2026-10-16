package com.windingmaster.presentation.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windingmaster.domain.model.MotorProfile
import com.windingmaster.domain.repository.MotorRepository
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
class MotorListViewModel @Inject constructor(
    private val repository: MotorRepository
) : ViewModel() {
    val motors: StateFlow<List<MotorProfile>> =
        repository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
}

data class MotorEditUiState(
    val motor: MotorProfile = MotorProfile(name = ""),
    val nameMissing: Boolean = false,
    val loading: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class MotorEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MotorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MotorEditUiState())
    val state: StateFlow<MotorEditUiState> = _state.asStateFlow()

    init {
        val id = savedStateHandle.get<Long>("motorId")?.takeIf { it >= 0 }
        viewModelScope.launch {
            val existing = id?.let { repository.getById(it) }
            _state.update { it.copy(motor = existing ?: MotorProfile(name = ""), loading = false) }
        }
    }

    fun edit(block: (MotorProfile) -> MotorProfile) =
        _state.update { it.copy(motor = block(it.motor), nameMissing = false) }

    fun save() {
        val motor = _state.value.motor
        if (motor.name.isBlank()) {
            _state.update { it.copy(nameMissing = true) }
            return
        }
        viewModelScope.launch {
            repository.save(motor.copy(name = motor.name.trim()))
            _state.update { it.copy(saved = true) }
        }
    }
}
