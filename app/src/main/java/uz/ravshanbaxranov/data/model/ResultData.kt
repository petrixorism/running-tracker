package uz.ravshanbaxranov.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResultData(
    val time: String,
    val distance: String
) : Parcelable