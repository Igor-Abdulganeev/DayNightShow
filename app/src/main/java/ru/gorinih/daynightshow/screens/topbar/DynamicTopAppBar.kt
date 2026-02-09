package ru.gorinih.testkotlindatelib.screens.topappbars

import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import ru.gorinih.daynightshow.navigation.NavKeys

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicTopAppBar(
    selected: Set<Int>,
    currentNavKey: NavKey,
    modifier: Modifier = Modifier,
    onRoute: (NavKeys) -> Unit,
    onBack: () -> Unit,
) {
    val hasSelection = selected.isNotEmpty()
    val topBarText = if (hasSelection) {
        "Выбрано ${selected.count()} эффектов"
    } else {
        "Погода в окне"
    }
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = { Text(text = topBarText) },
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = modifier,
        navigationIcon = {
            if (currentNavKey !is NavKeys.HomeScreen) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                }
            }
        },
        actions = {
            if (currentNavKey is NavKeys.HomeScreen) {
                IconButton(
                    onClick = {
                        onRoute(NavKeys.DayNightScreen)
                    }
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Старт шоу")
                }
                IconButton(
                    onClick = {
                        showMenu = true
                    },
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                }
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = {
                    showMenu = false
                }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Смена дня и ночи"
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRoute(NavKeys.DayNightScreen)
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Фейерверк"
                        )
                    },
                    onClick = {
                        showMenu = false
                        onRoute(NavKeys.FireworkScreen)
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "AGSL Ripple"
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRoute(NavKeys.AGSLRippleScreen)
                        }
                    )
                }
            }
        },
    )
}