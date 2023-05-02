package uz.ravshanbaxranov.data.model

import com.github.mikephil.charting.data.Entry
import uz.ravshanbaxranov.distancetracker.R

data class ChartType(
    val lineValues: List<Entry> = emptyList(),
    val chartColor: Int = R.color.purple,
    val chartDrawable: Int = R.drawable.blue_gradient
)
