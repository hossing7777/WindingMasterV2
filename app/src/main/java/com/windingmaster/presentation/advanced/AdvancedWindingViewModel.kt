package com.windingmaster.presentation.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windingmaster.domain.model.PitchMode
import com.windingmaster.domain.model.SavedCalculation
import com.windingmaster.domain.model.ValidationError
import com.windingmaster.domain.model.WindingInput
import com.windingmaster.domain.model.WindingLayout
import com.windingmaster.domain.model.WindingResult
import com.windingmaster.domain.model.WindingType
import com.windingmaster.domain.repository.CalculationRepository
import com.windingmaster.domain.usecase.CalcOutcome
import com.windingmaster.domain.usecase.CalculateWindingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdvancedUiState(
    val slots: String = "24",
    val poles: Int = 4,
    val frequency: String = "50",
    val phases: Int = 3,
    val pitchMode: PitchMode = PitchMode.FULL,
    val layout: WindingLayout = WindingLayout.CONCENTRIC,
    val customPitch: String = "",
    val result: WindingResult? = null,
    val error: ValidationError? = null,
    val selectedSlot: Int? = null,
    val lastSavedName: String? = null
)

@HiltViewModel
class AdvancedWindingViewModel @Inject constructor(
    private val calculateWinding: CalculateWindingUseCase,
    private val calculationRepository: CalculationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdvancedUiState().recompute())
    val state: StateFlow<AdvancedUiState> = _state.asStateFlow()

    val savedCalculations: StateFlow<List<SavedCalculation>> =
        calculationRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSlotsChange(v: String) =
        change { copy(slots = v.filter(Char::isDigit).take(3), customPitch = "", selectedSlot = null) }
    fun onPolesChange(v: Int) = change { copy(poles = v, customPitch = "", selectedSlot = null) }
    fun onFrequencyChange(v: String) = change { copy(frequency = v.filter(Char::isDigit).take(3)) }
    fun onPhasesChange(v: Int) = change { copy(phases = v, selectedSlot = null) }
    fun onPitchModeChange(v: PitchMode) = change { copy(pitchMode = v, customPitch = "") }
    fun onLayoutChange(v: WindingLayout) = change { copy(layout = v, customPitch = "") }
    fun onPitchChange(v: String) = change { copy(customPitch = v.filter(Char::isDigit).take(3)) }
    fun onResetPitch() = change { copy(customPitch = "") }
    fun onSlotSelected(slot: Int?) = _state.update { it.copy(selectedSlot = slot) }

    fun save(name: String) {
        val s = _state.value
        val input = s.toInput() ?: return
        if (s.result == null) return
        val finalName = name.trim().ifEmpty { "${input.slots}/${input.poles} (پیشرفته)" }
        viewModelScope.launch {
            calculationRepository.save(
                SavedCalculation(
                    name = finalName, slots = input.slots, poles = input.poles,
                    frequency = input.frequency, phases = input.phases,
                    windingType = WindingType.FULL_CONCENTRIC,
                    coilPitch = input.coilPitch
                )
            )
            _state.update { it.copy(lastSavedName = finalName) }
        }
    }

    fun consumeSaved() = _state.update { it.copy(lastSavedName = null) }
    fun deleteSaved(id: Long) = viewModelScope.launch { calculationRepository.delete(id) }

    private fun change(block: AdvancedUiState.() -> AdvancedUiState) {
        _state.update { it.block().recompute() }
    }

    private fun AdvancedUiState.toInput(): WindingInput? {
        val z = slots.toIntOrNull() ?: return null
        val f = frequency.toIntOrNull() ?: return null
        return WindingInput(
            slots = z, poles = poles, frequency = f, phases = phases,
            pitchMode = pitchMode, layout = layout,
            coilPitch = customPitch.toIntOrNull()
        )
    }

    private fun AdvancedUiState.recompute(): AdvancedUiState {
        val input = toInput()
            ?: return copy(result = null, error = ValidationError.SLOTS_OUT_OF_RANGE, selectedSlot = null)
        return when (val o = calculateWinding(input)) {
            is CalcOutcome.Success -> copy(result = o.result, error = null)
            is CalcOutcome.Invalid -> copy(result = null, error = o.error, selectedSlot = null)
        }
    }
}
