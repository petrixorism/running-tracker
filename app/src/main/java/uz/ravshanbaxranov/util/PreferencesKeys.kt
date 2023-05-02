package uz.ravshanbaxranov.util

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey


object PreferencesKeys {

    val WEEKLY_GOAL = intPreferencesKey("weekly_goal")
    val WEIGHT = intPreferencesKey("total_average_speed")
    val FULL_NAME = stringPreferencesKey("full_name")
    val MAP_STYLE = stringPreferencesKey("map_style")
}