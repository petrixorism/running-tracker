package uz.ravshanbaxranov.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.db.RunDao
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.domain.usecase.RunUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val useCase: RunUseCase
) : ViewModel() {

    private val _historyListChannel = MutableStateFlow<List<Run>>(emptyList())
    val historyListFlow: Flow<List<Run>> = _historyListChannel.asStateFlow()

    init {
        viewModelScope.launch {
            useCase.getRuns.getAllRuns().collect {
                _historyListChannel.emit(it)
            }
        }
    }

}