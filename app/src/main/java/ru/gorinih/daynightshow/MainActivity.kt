package ru.gorinih.daynightshow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ru.gorinih.daynightshow.navigation.NavKeys
import ru.gorinih.daynightshow.screens.daynight.DayNightView
import ru.gorinih.daynightshow.screens.details.DetailsOfProducts
import ru.gorinih.daynightshow.screens.firework.FireworkSimpleScreen
import ru.gorinih.daynightshow.screens.main.ListSplitScreen
import ru.gorinih.daynightshow.screens.main.Products
import ru.gorinih.daynightshow.ui.theme.DayNightShowTheme
import ru.gorinih.daynightshow.screens.agsl.AgslRippleDemoScreen
import ru.gorinih.testkotlindatelib.screens.topappbars.DynamicTopAppBar

val testListProducts = listOf(
    Products(
        id = 1,
        name = "Проход солнца",
        icon = "Sun",
        description = "Включает проход солнца по небу"
    ),
    Products(
        id = 2,
        name = "Проход луны",
        icon = "Moon",
        description = "Включает проход луны по небу"
    ),
    Products(id = 3, name = "Облака", icon = "Cloud", description = "Включает облака"),
    Products(id = 4, name = "Фейервер", icon = "Firework", description = "Включает фейерверки"),
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayNightShowTheme {
                val scrollBehavior =
                    TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val listItems by remember { mutableStateOf(testListProducts) }
                var selectedItems by rememberSaveable { mutableStateOf(setOf<Int>()) }
                val backStackNav = rememberNavBackStack(NavKeys.HomeScreen)
                val strategy = rememberListDetailSceneStrategy<NavKey>()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        DynamicTopAppBar(
                            selectedItems, currentNavKey = backStackNav.last(), Modifier,
                            onBack = {
                                when (backStackNav.count() > 1) {
                                    true -> backStackNav.removeLastOrNull()
                                    false -> finishAfterTransition()
                                }
                            },
                            onRoute = { key ->
                                backStackNav.add(key)
                            })
                    }
                ) { innerPadding ->

                    Surface(modifier = Modifier.padding(innerPadding)) {
/*
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            //AnimateFirework()
                            RunFirework()

                            /*
                                                                                AgslStarDemoScreen()
                            */
                            /*
                                                    AgslRippleDemoScreen()
                            */
                            /*
                                                    AgslGradientDemoScreen()
                            */
                        } else {
                            */
                        NavDisplay(
                            backStack = backStackNav,
                            onBack = { backStackNav.removeLastOrNull() },
                            sceneStrategy = strategy,
                            entryProvider = entryProvider {
                                entry<NavKeys.HomeScreen>(
                                    metadata = ListDetailSceneStrategy.listPane(
                                        detailPlaceholder = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(color = Color.Yellow)
                                            ) {
                                                Text(text = "Выберите раздел")
                                            }
                                        }
                                    )
                                ) {
                                    ListSplitScreen(
                                        modifier = Modifier,
                                        initialRatio = 0.5f,
                                        listItems,
                                        selectedItems,
                                        backStackNav
                                    ) { selectId ->
                                        val doIt = selectedItems.contains(selectId)
                                        when (doIt) {
                                            true -> selectedItems -= selectId
                                            false -> selectedItems += selectId
                                        }
                                    }
                                }
                                detailsEntry()
                                daynightEntry()
                                fireworkEntry()
                                entry<NavKeys.AGSLRippleScreen> {
                                    AgslRippleDemoScreen()
                                }
                            },
                            transitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { it }
                                ) togetherWith
                                        slideOutHorizontally(targetOffsetX = { -it })
                            },
                            popTransitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { -it }
                                ) togetherWith
                                        slideOutHorizontally { it }
                            },
                            predictivePopTransitionSpec = {
                                slideInHorizontally(
                                    initialOffsetX = { -it }
                                ) togetherWith
                                        slideOutHorizontally { it }
                            }
                        )
                    }
                }

                /*
                                }
                */
            }
        }
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    private fun EntryProviderScope<NavKey>.detailsEntry() {
        entry<NavKeys.DetailsScreen>(
            metadata = ListDetailSceneStrategy.detailPane()
                    + NavDisplay.transitionSpec {
                slideIntoContainer(
                    // новый экран как угодно настраивается с какой стороны лететь
                    initialOffset = { it },
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(1000),
                ) togetherWith
                        ExitTransition.KeepUntilTransitionsFinished // старый остается на месте
            } + NavDisplay.popTransitionSpec {
                EnterTransition.None togetherWith // старый сверху вниз
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(1000)
                        )
            } + NavDisplay.predictivePopTransitionSpec {
                EnterTransition.None togetherWith
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(1000)
                        )
            }
        ) { key ->
            DetailsOfProducts(key.detail)
        }
    }

    private fun EntryProviderScope<NavKey>.daynightEntry() {
        entry<NavKeys.DayNightScreen> {
            DayNightView()
        }
    }

    private fun EntryProviderScope<NavKey>.fireworkEntry() {
        entry<NavKeys.FireworkScreen> {
            FireworkSimpleScreen()
        }
    }
}
