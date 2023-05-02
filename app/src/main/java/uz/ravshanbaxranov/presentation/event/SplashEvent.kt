package uz.ravshanbaxranov.presentation.event

sealed class SplashEvent {
    data class SetNameAndWeight(val name: String, val weight: String) : SplashEvent()
}