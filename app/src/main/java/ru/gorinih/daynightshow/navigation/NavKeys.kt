package ru.gorinih.daynightshow.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.gorinih.daynightshow.screens.main.Products

@Serializable
sealed interface NavKeys : NavKey {
    @Serializable
    data object HomeScreen : NavKeys

    @Serializable
    data class DetailsScreen(val detail: Products) : NavKeys

    @Serializable
    data object DayNightScreen : NavKeys

    @Serializable
    data object FireworkScreen : NavKeys

    @Serializable
    data object AGSLRippleScreen : NavKeys

}

