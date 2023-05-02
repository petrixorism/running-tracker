package uz.ravshanbaxranov.presentation.viewmodel

import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.domain.usecase.RunUseCase
import uz.ravshanbaxranov.util.Constants.STANDARD
import uz.ravshanbaxranov.util.MapsUtil
import uz.ravshanbaxranov.util.PreferencesKeys.MAP_STYLE
import uz.ravshanbaxranov.util.PreferencesKeys.WEIGHT
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val useCase: RunUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val mapUtil by lazy { MapsUtil() }

    private val _mapStyleChannel = Channel<String>()
    val mapStyleFlow = _mapStyleChannel.receiveAsFlow()


    init {
        viewModelScope.launch {
            dataStore.data.collect {
                _mapStyleChannel.send(it[MAP_STYLE] ?: STANDARD)
            }
        }


    }

    fun insertRun(elapsedTime: Long, distance: Int, bitmap: Bitmap?) {

        viewModelScope.launch {
            dataStore.data.collect { pref ->
                val weight = pref[WEIGHT] ?: 0
                val avgSpeed = (distance / 1000.0f) / (elapsedTime / (1000 * 60 * 60.0f))
                val calorie = mapUtil.calculateCaloriesBurned(distance, weight)

                useCase.addRun(
                    Run(
                        image = bitmap,
                        time = elapsedTime,
                        distanceInMeters = distance,
                        averageSpeed = avgSpeed,
                        caloriesBurned = calorie,
                    )
                )
            }
        }


    }


    fun changeMapStyle(style: String) {
        viewModelScope.launch {
            dataStore.edit {
                it[MAP_STYLE] = style
            }
        }
    }

}

