package uz.ravshanbaxranov.data.model

import android.graphics.Color
import uz.ravshanbaxranov.distancetracker.R

data class MapTheme(
    val style: Int = R.raw.standard_mapstyle,
    val icon: Int = R.raw.standart,
    val textColor: Int = R.color.standard_style_text_color
)
