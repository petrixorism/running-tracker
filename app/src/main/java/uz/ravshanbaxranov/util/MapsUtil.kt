package uz.ravshanbaxranov.util

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import uz.ravshanbaxranov.data.model.MapTheme
import uz.ravshanbaxranov.distancetracker.R

class MapsUtil {

    fun countTotalTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = stopTime - startTime
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
        return "${hours}h ${minutes}m ${seconds}s"
    }


    fun calculateCaloriesBurned(meters: Int, weight:Int): Int {
        return (meters / 1000.0 * weight * 0.95).toInt()
    }

    fun showBiggerMap(map: GoogleMap, locations: List<LatLng>) {
        val bound = LatLngBounds.Builder()
        locations.forEach {
            bound.include(it)
        }
        if (locations.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bound.build(), 200),
                1000,
                null
            )
        }
    }

    fun calculatePolylineLength(polyline: List<LatLng>): Int {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]

            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance.toInt()
    }

    fun calculateAvgSpeed(latLongList: List<LatLng>, startTime: Long): Float {
        return (calculatePolylineLength(latLongList) / 1000.0f) / ((System.currentTimeMillis() - startTime) / (1000 * 60 * 60.0f))
    }

    @SuppressLint("ResourceType")
    fun changeMapStyle(
        style: String
    ): MapTheme {
        return when (style) {
            Constants.STANDARD -> {
                MapTheme()
            }
            Constants.SILVER -> {
                MapTheme(
                    style = R.raw.silver_mapstyle,
                    icon = R.raw.silver,
                    textColor = R.color.silver_style_text_color
                )
            }
            Constants.DARK -> {
                MapTheme(
                    style = R.raw.dark_mapstyle,
                    icon = R.raw.dark,
                    textColor = R.color.dark_style_text_color
                )
            }
            Constants.NIGHT -> {
                MapTheme(
                    style = R.raw.night_mapstyle,
                    icon = R.raw.night,
                    textColor = R.color.night_style_text_color
                )
            }
            Constants.RETRO -> {
                MapTheme(
                    style = R.raw.retro_mapstyle,
                    icon = R.raw.retro,
                    textColor = R.color.retro_style_text_color
                )
            }
            Constants.AUBERGINE -> {
                MapTheme(
                    style = R.raw.aubergine_mapstyle,
                    icon = R.raw.aubergine,
                    textColor = R.color.aubergine_style_text_color
                )
            }
            else -> {
                MapTheme()
            }
        }
    }
}