package uz.ravshanbaxranov.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.data.db.RunDao
import uz.ravshanbaxranov.data.model.TotalData
import uz.ravshanbaxranov.data.model.WeeklyGoal
import uz.ravshanbaxranov.domain.RunRepository
import uz.ravshanbaxranov.util.PreferencesKeys.WEEKLY_GOAL
import java.text.DecimalFormat
import java.util.*


class RunRepositoryImpl(
    private val dao: RunDao,
    private val dataStore: DataStore<Preferences>
) :
    RunRepository {

    override suspend fun deleteRun(run: Run) {
        dao.deleteRun(run)
    }

    override suspend fun addRun(run: Run) {
        if (dao.getOneItem(run.averageSpeed) == null)
            dao.insertWordInfo(run)
    }

    override fun getRuns(): Flow<List<Run>> = dao.getRuns()

    override suspend fun getTotalAndAverageData(): TotalData {

        var totalData = TotalData(
            duration = DecimalFormat("##.##").format(dao.getSumDuration() / 1000.0f / 60 / 60) + " h",
            distance = DecimalFormat("##.##").format(dao.getSumDistance() / 1000.0f) + " km",
            calories = "${dao.getSumCalories()} kcal",
            avgSpeed = "0 km/h"
        )

        val avgSpeed =
            (dao.getSumDistance() / 1000.0) / (dao.getSumDuration() / 1000.0f / 60 / 60)

        if (avgSpeed > 0) {
            totalData = totalData.copy(
                avgSpeed = DecimalFormat("##.##").format(avgSpeed) + " km/h"
            )
        }
        return totalData
    }

    override suspend fun getWeeklyGoal(): Flow<WeeklyGoal> = flow {


        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek


        dataStore.data.collect { dataStore ->
            emit(
                WeeklyGoal(
                    goal = dataStore[WEEKLY_GOAL] ?: 5,
                    done = dao.getWeeklyRunData(cal.timeInMillis) / 1000.0f
                )
            )
        }
    }

    override suspend fun setWeeklyGoal(km: Int) {
        dataStore.edit {
            it[WEEKLY_GOAL] = km
        }
    }


}