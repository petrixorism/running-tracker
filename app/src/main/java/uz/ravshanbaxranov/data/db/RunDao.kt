package uz.ravshanbaxranov.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp
import java.util.Date

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWordInfo(info: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT SUM(distanceInMeters) FROM run_table WHERE date > :mondayTimestamp")
    fun getWeeklyRunData(mondayTimestamp: Long): Float

    @Query("SELECT * FROM run_table")
    fun getRuns(): Flow<List<Run>>

    @Query("SELECT AVG(averageSpeed) FROM run_table")
    fun getAverageSpeed(): Float

    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getSumDistance(): Int

    @Query("SELECT SUM(time) FROM run_table")
    fun getSumDuration(): Long

    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getSumCalories(): Int

    @Query("select * from run_table where averageSpeed=:averageSpeed")
    fun getOneItem(averageSpeed: Float): Run?

}