package uz.ravshanbaxranov.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.presentation.event.SplashEvent
import uz.ravshanbaxranov.util.PreferencesKeys.FULL_NAME
import uz.ravshanbaxranov.util.PreferencesKeys.WEIGHT
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {


    private val _eventFlow = Channel<UiEvent>()
    val eventFlow: Flow<UiEvent> = _eventFlow.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect {
                val name = it[FULL_NAME] ?: ""
                val weight = it[WEIGHT] ?: 0
                if (name.isEmpty() || weight == 0) {
                    _eventFlow.send(UiEvent.OpenBottomSheet)
                } else {
                    delay(2000L)
                    _eventFlow.send(UiEvent.NavigateHome)
                }
            }
        }
    }

    fun setNameAndWeight(event: SplashEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is SplashEvent.SetNameAndWeight -> {
                    if (event.name.length < 3) {
                        _eventFlow.send(UiEvent.Toast("Please enter your name correctly!"))
                    } else if (event.weight.toInt() < 10) {
                        _eventFlow.send(UiEvent.Toast("Enter your weight correctly"))
                    } else {
                        dataStore.edit {
                            it[WEIGHT] = event.weight.toInt()
                            it[FULL_NAME] = event.name
                        }
                    }
                }
            }
        }
    }

}

sealed class UiEvent() {
    object OpenBottomSheet : UiEvent()
    object NavigateHome : UiEvent()
    data class Toast(val message: String) : UiEvent()
}