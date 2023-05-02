package uz.ravshanbaxranov.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.ravshanbaxranov.data.model.ChartType
import uz.ravshanbaxranov.data.model.TotalData
import uz.ravshanbaxranov.data.model.WeeklyGoal
import uz.ravshanbaxranov.distancetracker.R
import uz.ravshanbaxranov.domain.usecase.RunUseCase
import uz.ravshanbaxranov.util.PreferencesKeys
import uz.ravshanbaxranov.util.PreferencesKeys.FULL_NAME
import uz.ravshanbaxranov.util.PreferencesKeys.WEEKLY_GOAL
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCase: RunUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _toastChannel = Channel<String>()
     val toastFlow = _toastChannel.receiveAsFlow()

    private val _chartDataList = MutableStateFlow(ChartType())
    val chartData: Flow<ChartType> = _chartDataList.asStateFlow()

    private val _nameChannel = MutableStateFlow("")
    val nameFlow: Flow<String> = _nameChannel.asStateFlow()

    private val _totalDataChannel = MutableStateFlow(TotalData())
    val totalDataFlow: Flow<TotalData> = _totalDataChannel.asStateFlow()

    private val _goalChannel = MutableStateFlow(WeeklyGoal())
    val goalFlow = _goalChannel.asStateFlow()

    private var chart = Chart()


    init {
        viewModelScope.launch {
            useCase.getRuns.getWeekly().collect {
                _goalChannel.emit(it)
            }
        }
        dataStore.data.onEach {
            val name = it[FULL_NAME] ?: ""
            _nameChannel.emit(name)
        }.launchIn(viewModelScope)

        chartType("Meters")

    }

    fun getTotalData() {

        viewModelScope.launch {
            _totalDataChannel.emit(useCase.getTotalAndAvgData())
        }

    }

    fun selectChartDuration(duration: String) {
        viewModelScope.launch {
            chart = chart.copy(duration = getTimestamp(duration))
            chartType(chart.type)
        }
    }

    fun chartType(type: String) {
        viewModelScope.launch {
            chart = chart.copy(type = type)
            useCase.getRuns.getAllRuns().collect { runList ->
                when (chart.type) {
                    "Minutes" -> {
                        _chartDataList.emit(ChartType(
                            lineValues = runList.filter { run ->
                                run.date > chart.duration
                            }.map { run ->
                                run.fromMinutesToEntry()
                            },
                            chartColor = R.color.blue,
                            chartDrawable = R.drawable.blue_gradient
                        )
                        )
                    }

                    "Km/hour" -> {
                        _chartDataList.emit(ChartType(
                            lineValues = runList.filter { run ->
                                run.date > chart.duration
                            }.map { run ->
                                run.fromSpeedToEntry()
                            },
                            chartColor = R.color.orange,
                            chartDrawable = R.drawable.orange_gradient
                        )
                        )
                    }

                    "Kcal" -> {
                        _chartDataList.emit(ChartType(
                            lineValues = runList.filter { run ->
                                run.date > chart.duration
                            }.map { run ->
                                run.fromKcalToEntry()
                            },
                            chartColor = R.color.red,
                            chartDrawable = R.drawable.red_gradient
                        )
                        )
                    }

                    "Meters" -> {
                        _chartDataList.emit(ChartType(
                            lineValues = runList.filter { run ->
                                run.date > chart.duration
                            }.map { run ->
                                run.fromDistanceToEntry()
                            },
                            chartColor = R.color.green,
                            chartDrawable = R.drawable.green_gradient
                        )
                        )
                    }
                }

            }
        }
    }

    fun changeWeeklyGoal(goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit {
                it[WEEKLY_GOAL] = goal
            }
        }
    }

    private fun getTimestamp(duration: String): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)



        when (duration) {
            "Total" -> return 0
            "Today" -> {
                cal[Calendar.HOUR_OF_DAY] = cal.firstDayOfWeek
            }

            "Week" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
            }

            "Month" -> {
                cal[Calendar.DAY_OF_MONTH] = cal.firstDayOfWeek
            }

            "Year" -> {
                cal[Calendar.DAY_OF_YEAR] = cal.firstDayOfWeek
            }
        }
        return cal.timeInMillis

    }

    fun setNameAndWeight(name: String, weight: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (name.length < 3) {
                _toastChannel.send("Please enter your name correctly!")
            } else if (weight.toInt() < 10) {
                _toastChannel.send("Enter your weight correctly")
            } else {
                dataStore.edit {
                    it[PreferencesKeys.WEIGHT] = weight.toInt()
                    it[FULL_NAME] = name
                }
            }
        }
    }


}

data class Chart(
    val type: String = "Meters",
    val duration: Long = 0
)
