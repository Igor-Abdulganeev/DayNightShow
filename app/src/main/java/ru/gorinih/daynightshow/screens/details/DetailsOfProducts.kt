package ru.gorinih.daynightshow.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.gorinih.daynightshow.screens.main.Products

@Composable
fun DetailsOfProducts(
    details: Products
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(text = "Переход по id = ${details.id}")
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = details.description)
        }
    }
}