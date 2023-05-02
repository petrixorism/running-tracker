package uz.ravshanbaxranov.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.db.RunDao
import uz.ravshanbaxranov.data.db.Run
import javax.inject.Inject

@HiltViewModel
class RunDetailViewModel
@Inject constructor(
    private val dao: RunDao
) : ViewModel() {

    private val _deletedChannel = Channel<Unit>()
    val deletedFlow: Flow<Unit> = _deletedChannel.receiveAsFlow()

    fun deleteRunDetail(run: Run) {
        viewModelScope.launch {
            dao.deleteRun(run)
            _deletedChannel.send(Unit)
        }
    }

}