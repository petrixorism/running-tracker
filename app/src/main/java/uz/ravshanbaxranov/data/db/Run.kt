package uz.ravshanbaxranov.data.db

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.mikephil.charting.data.Entry
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "run_table")
data class Run(
    @PrimaryKey
    val id: Int? = null,
    val image: Bitmap? = null,
    val time: Long,
    val distanceInMeters: Int,
    val averageSpeed: Float,
    val caloriesBurned: Int,
    val date: Long = System.currentTimeMillis()
) : Parcelable {
    fun fromDistanceToEntry(): Entry {
        return Entry(
            id?.toFloat() ?: 0.0f, distanceInMeters.toFloat()
        )
    }

    fun fromKcalToEntry(): Entry {
        return Entry(
            id?.toFloat() ?: 0.0f, caloriesBurned.toFloat()
        )
    }

    fun fromSpeedToEntry(): Entry {
        return Entry(
            id?.toFloat() ?: 0.0f, averageSpeed.toFloat()
        )
    }

    fun fromMinutesToEntry(): Entry {
        return Entry(
            id?.toFloat() ?: 0.0f, (time / 1000.0f / 60)
        )
    }

    fun toRunEntity(): uz.ravshanbaxranov.data.db.Run {
        return uz.ravshanbaxranov.data.db.Run(
            id, image, time, distanceInMeters, averageSpeed, caloriesBurned, date
        )
    }
}