package ru.gorinih.daynightshow.screens.main

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Fitbit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import ru.gorinih.daynightshow.navigation.NavKeys

@Composable
fun PopUpLayout(text: String) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(100))
            .background(Color(0xFFA8DEFF))
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .clip(shape = RoundedCornerShape(100))
                .background(Color(0xFF0091EA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(5.dp),
                text = text,
                color = Color.White
            )
        }
    }

}

@SuppressLint("SuspiciousIndentation", "RememberReturnType")
@Composable
fun ListSplitScreen(
    modifier: Modifier = Modifier,
    initialRatio: Float = 0.75f,
    listItems: List<Products>,
    selectedItems: Set<Int>,
    backStackNav: NavBackStack<NavKey>,
    setSelectedItems: (Int) -> Unit,
) {
    var ratio by remember { mutableFloatStateOf(initialRatio) } // коэффициент соотношения
    val brushUp = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF9C4),
            Color(0xFFF8F7E9),
        )
    )
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            Modifier
                .weight(ratio)
                .fillMaxWidth()
                .background(brush = brushUp)
        ) {
            ListProducts(
                listData = listItems,
                Modifier,
                setSelectedItems,
                selectedItems,
                backStackNav
            )
        }
    }
}

@Composable
fun ListProducts(
    listData: List<Products>,
    modifier: Modifier = Modifier,
    setSelectedItems: (Int) -> Unit = {},
    selectedItems: Set<Int> = emptySet(),
    backStackNav: NavBackStack<NavKey>
) {

    val stateList = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    LazyColumn(state = stateList) {
        items(listData, key = { it.id }) { item ->
            val isSelect = selectedItems.contains(item.id)
            Column {
                Card(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconView = item.getIcon()
                        IconButton(
                            onClick = { backStackNav.add(NavKeys.DetailsScreen(item)) },
                            content = {
                                Icon(imageVector = iconView, contentDescription = null)
                            },
                        )

                        Spacer(modifier = Modifier.width(16.dp))
                        Checkbox(
                            checked = isSelect,
                            onCheckedChange = {
                                setSelectedItems(item.id)
                            },
                            checkmarkStroke = Stroke(width = 10f, cap = StrokeCap.Round),
                            outlineStroke = Stroke(width = 4f, cap = StrokeCap.Butt),
                            enabled = true,
                        )
                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun PreviewPopUpLayout() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        PopUpLayout("Проверка текста")
    }
}

@Serializable
data class Products(
    val id: Int,
    val name: String,
    val icon: String,
    val description: String = ""
) {
    fun getIcon(): ImageVector = when (icon) {
        "Sun" -> Icons.Default.Adjust
        "Cloud" -> Icons.Default.Cloud
        "Moon" -> Icons.Default.Album
        "Firework" -> Icons.Default.Fitbit
        else -> Icons.Default.Language
    }
}
