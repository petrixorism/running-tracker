package uz.ravshanbaxranov.domain.usecase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.data.model.WeeklyGoal
import uz.ravshanbaxranov.domain.RunRepository


class GetRuns(
    private val repository: RunRepository) {

    fun getAllRuns(): Flow<List<Run>> = repository.getRuns()

    suspend fun getWeekly(): Flow<WeeklyGoal> = repository.getWeeklyGoal()




}