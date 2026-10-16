package com.windingmaster.presentation.checklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windingmaster.domain.model.ChecklistItem
import com.windingmaster.domain.model.ChecklistTemplates
import com.windingmaster.domain.repository.ChecklistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ChecklistRepository
) : ViewModel() {

    val motorId: Long? = savedStateHandle.get<Long>("motorId")?.takeIf { it >= 0 }

    val items: StateFlow<List<ChecklistItem>> =
        repository.observe(motorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.seedIfEmpty(motorId, ChecklistTemplates.repairKeys) }
    }

    fun toggle(item: ChecklistItem) =
        viewModelScope.launch { repository.setDone(item.id, !item.isDone) }
    fun addCustom(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { repository.addCustom(motorId, text) }
    }
    fun delete(id: Long) = viewModelScope.launch { repository.delete(id) }
    fun resetAll() = viewModelScope.launch { repository.resetAll(motorId) }
}
