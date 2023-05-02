package uz.ravshanbaxranov.domain

import kotlinx.coroutines.flow.Flow
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.data.model.TotalData
import uz.ravshanbaxranov.data.model.WeeklyGoal


interface RunRepository {

    suspend fun deleteRun(run: Run)

    suspend fun addRun(run: Run)

    fun getRuns(): Flow<List<Run>>

    suspend fun getTotalAndAverageData(): TotalData

    suspend fun getWeeklyGoal():Flow<WeeklyGoal>

    suspend fun setWeeklyGoal(km: Int)
}