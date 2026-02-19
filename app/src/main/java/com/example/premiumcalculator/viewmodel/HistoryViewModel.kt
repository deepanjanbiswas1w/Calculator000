package com.example.premiumcalculator.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premiumcalculator.data.HistoryEntity
import com.example.premiumcalculator.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _history = mutableStateOf<List<HistoryEntity>>(emptyList())
    val history: State<List<HistoryEntity>> = _history

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getAll()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _history.value = emptyList()
        }
    }

    fun updateNote(item: HistoryEntity, note: String) {
        viewModelScope.launch {
            val updatedItem = item.copy(note = note)
            repository.update(updatedItem)
            loadHistory()
        }
    }
}
