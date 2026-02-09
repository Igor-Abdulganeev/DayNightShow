package ru.gorinih.daynightshow.screens.daynight

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import ru.gorinih.daynightshow.screens.firework.FireworkRunning
import kotlin.math.*
import kotlin.random.Random

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DayNightView() {

    val infiniteTransition = rememberInfiniteTransition(label = "day_night")
    val speed = 32000
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = speed,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycleAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(LocalConfiguration.current.screenHeightDp.dp / 2)
    ) {
        DayNightBackground(cycleProgress = cycleProgress)
        val isShow = cycleProgress > 0.5f && cycleProgress < 0.8f
        FireworkRunning(isShow)
    }
}


data class NightStar(
    val x: Float,
    val y: Float,
    val size: Float,
    val twinklePhase: Float,
    val twinkleSpeed: Float,
)

data class Cloud(val xOffset: Float, val y: Float, val scale: Float, val speed: Float)

private const val STARS_COUNT = 60

enum class PhaseDay {
    SunRise,
    Day,
    SunSet,
    Night,
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun DayNightBackground(cycleProgress: Float, modifier: Modifier = Modifier) {
    val isDay = cycleProgress < 0.5f
    val dayProgress = if (isDay) cycleProgress * 2f else (cycleProgress - 0.5f) * 2f
    var totalTime by remember { mutableFloatStateOf(0f) }
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    val sunriseColors = listOf(Color(0xFFFF8C42), Color(0xFFFFD700), Color(0xFFFFF4E0))
    val dayColors = listOf(Color(0xFF4A90D9), Color(0xFF87CEEB), Color(0xFFB8E0F0))
    val sunsetColors = listOf(Color(0xFFFF6B35), Color(0xFFFFAB5E), Color(0xFFFFD89E))
    val duskColors = listOf(Color(0xFF2C3E50), Color(0xFF34495E), Color(0xFF5D6D7E))
    val nightColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF2C3E50))
//    val nightColors = listOf(Color(0xFF050C11), Color(0xFF0D151E), Color(0xFF12191F))
    val dawnColors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF1F4068))
    var phaseDay by remember { mutableStateOf(PhaseDay.SunRise) }

    val stars = remember {
        List(STARS_COUNT) {
            NightStar(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.6f,
                size = Random.nextFloat() * 2f + 1f,
                twinklePhase = Random.nextFloat() * PI.toFloat() * 2f,
                twinkleSpeed = Random.nextFloat() * 2f + 1f
            )
        }
    }

    val clouds = remember {
        List(5) {
            Cloud(
                xOffset = Random.nextFloat(),
                y = Random.nextFloat() * 0.3f + 0.1f,
                scale = Random.nextFloat() * 0.5f + 0.8f,
                speed = Random.nextFloat() * 0.02f + 0.01f,
            )
        }
    }


    val skyColors = if (isDay) {
        when {
            dayProgress < 0.2f -> {
                phaseDay = PhaseDay.SunRise
                val timeColor = dayProgress / 0.2f
                //       Log.i("GINES", "SunRise to Day $timeColor") // alpha from 0.2 to 0
                listOf(
                    lerp(sunriseColors[0], dayColors[0], timeColor),
                    lerp(sunriseColors[1], dayColors[1], timeColor),
                    lerp(sunriseColors[2], dayColors[2], timeColor)
                )
            }

            dayProgress < 0.8f -> {
                phaseDay = PhaseDay.Day
                //       Log.d("GINES", "Day $dayProgress") // alpha 0
                dayColors
            }

            else -> {
                phaseDay = PhaseDay.SunSet
                val timeColor = (dayProgress - 0.8f) / 0.2f
                //   Log.e("GINES", "Day to SunSet $timeColor") // alpha from 0 to 0.5
                listOf(
                    lerp(dayColors[0], sunsetColors[0], timeColor),
                    lerp(dayColors[1], sunsetColors[1], timeColor),
                    lerp(dayColors[2], sunsetColors[2], timeColor),
                )
            }
        }
    } else {
        phaseDay = PhaseDay.Night
        when {
            dayProgress < 0.2f -> {
                val timeColor = dayProgress / 0.2f
//                Log.i("GINES", "SunSet to Dusk $timeColor ($dayProgress)") // alpha from 0.5 to 1
                listOf(
                    lerp(sunsetColors[0], duskColors[0], timeColor),
                    lerp(sunsetColors[1], duskColors[1], timeColor),
                    lerp(sunsetColors[2], duskColors[2], timeColor)
                )
            }

            dayProgress < 0.4f -> {
                val timeColor = (dayProgress - 0.2f) / 0.2f
//                Log.d("GINES", "Dusk to Night $timeColor ($dayProgress)") // alpha from 0.5 to 1
                listOf(
                    lerp(duskColors[0], nightColors[0], timeColor),
                    lerp(duskColors[1], nightColors[1], timeColor),
                    lerp(duskColors[2], nightColors[2], timeColor)
                )
            }

            dayProgress < 0.6f -> {
                //    Log.e("GINES", "Night $dayProgress") // aplha 1
                nightColors
            }

            dayProgress < 0.8f -> {
                val timeColor = (dayProgress - 0.6f) / 0.2f
//                Log.d("GINES", "Night to Dauw $timeColor ($dayProgress)") // alpha from 0.5 to 1
                listOf(
                    lerp(nightColors[0], dawnColors[0], timeColor),
                    lerp(nightColors[1], dawnColors[1], timeColor),
                    lerp(nightColors[2], dawnColors[2], timeColor)
                )
            }

            else -> {
                val timeColor = (dayProgress - 0.8f) / 0.2f
//                Log.i("GINES", "Dawn to SunRise $timeColor") // alpha from 1 to 0.2
                listOf(
                    lerp(dawnColors[0], sunriseColors[0], timeColor),
                    lerp(dawnColors[1], sunriseColors[1], timeColor),
                    lerp(dawnColors[2], sunriseColors[2], timeColor),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTimeNanos ->
                val deltaTime = when (lastFrameTimeNanos) {
                    0L -> 0.016f
                    else -> ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(
                        0f,
                        0.1f
                    )
                }
                lastFrameTimeNanos = frameTimeNanos
                totalTime += deltaTime
            }
        }
    }

    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        val width = size.width
        val height = size.height /// 2f

        //фон день-ночь
        drawRect(
            brush = Brush.verticalGradient(colors = skyColors),
            size = size
        )

        //звезды
        val starAlpha = when (isDay) {
            true -> 0f
            false -> {
                when {
                    dayProgress < 0.2f -> dayProgress * 6f
                    dayProgress > 0.8f -> 1f - (dayProgress - 0.8f) * 6f
                    else -> 1f
                }
            }
        }

        val shiftX = width + (width * 17f * dayProgress)

        if (starAlpha > 0f) {
            for (star in stars) {
                val splash = (sin(totalTime * star.twinkleSpeed + star.twinklePhase) + 1f) / 2f
                val alpha = starAlpha * (0.4f + splash * 0.6f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = star.size,
                    center = Offset(star.x * shiftX, star.y * height)
                )
            }
        }


        //солнце и луна
        val sunMoonY = height * 0.35f
        val arcRadius = width * 0.53f
        val centerX = width / 2f
        val moveAngle = PI.toFloat() * (1f - dayProgress * 0.95f)
        val moveX = centerX + cos(moveAngle) * arcRadius
        val moveY = sunMoonY - sin(moveAngle) * arcRadius * 0.4f + height * 0.1f

        when (phaseDay != PhaseDay.Night) {
            true -> {
                //солнце
                if (moveY < height * 0.7f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFCC).copy(alpha = 0.4f),
                                Color(0xFFFFD700).copy(alpha = 0.2f),
                                Color.Transparent,
                            ),
                            center = Offset(moveX, moveY),
                            radius = 80f,
                        ),
                        radius = 80f,
                        center = Offset(moveX, moveY),
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFE0),
                                Color(0xFFFFD700),
                                Color(0xFFFFA500),
                            ),
                            center = Offset(moveX - 8f, moveY - 8f),
                            radius = 35f,
                        ),
                        radius = 35f,
                        center = Offset(moveX, moveY),
                    )
                }
            }

            false -> {
                //луна
                val moonAlpha = when {
                    dayProgress < 0.3f -> dayProgress * 3f
                    dayProgress > 0.7f -> 1f - (dayProgress - 0.8f) * 6f
                    else -> 1f
                }
                if (moveY < height * 0.7f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF5F5DC).copy(alpha = 0.3f),
                                Color(0xFFE8E8D0).copy(alpha = 0.1f),
                                Color.Transparent,
                            ),
                            center = Offset(moveX, moveY),
                            radius = 60f,
                        ),
                        radius = 60f,
                        center = Offset(moveX, moveY),
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFF0).copy(alpha = moonAlpha),
                                Color(0xFFF5F5DC).copy(alpha = moonAlpha),
                                Color(0xFFE8E8D0).copy(alpha = moonAlpha),
                            ),
                            center = Offset(moveX - 5f, moveY - 5f),
                            radius = 28f,
                        ),
                        radius = 28f,
                        center = Offset(moveX, moveY),
                    )
                }
            }
        }

        //облака
        val cloudAlpha =
            when {
                dayProgress < 0.3 -> dayProgress * 3f
                dayProgress > 0.98f -> 1f - dayProgress * 0.95f
                dayProgress > 0.97f -> 1f - dayProgress * 0.9f
                dayProgress > 0.96f -> 1f - dayProgress * 0.8f
                dayProgress > 0.95f -> 1f - dayProgress * 0.7f
                dayProgress > 0.9f -> 1f - dayProgress * 0.6f
                dayProgress > 0.85f -> 1f - dayProgress * 0.5f
                dayProgress > 0.8f -> 1f - dayProgress * 0.4f
                dayProgress > 0.75f -> 1f - dayProgress * 0.3f
                dayProgress > 0.7f -> 1f - dayProgress * 0.2f //+0.1f
                else -> 1f
            }
        val cloudColor = if (isDay) Color.White else Color(0xFF555555)

        for (cloud in clouds) {
            val cloudX = ((cloud.xOffset + totalTime * cloud.speed) % 1.4f - 0.2f) * width
            val cloudY = cloud.y * height

            drawCircle(
                color = cloudColor.copy(alpha = cloudAlpha * 0.8f),
                radius = 25f * cloud.scale,
                center = Offset(cloudX, cloudY),
            )

            drawCircle(
                color = cloudColor.copy(alpha = cloudAlpha),
                radius = 35f * cloud.scale,
                center = Offset(cloudX + 30f * cloud.scale, cloudY - 5f),
            )
            drawCircle(
                color = cloudColor.copy(alpha = cloudAlpha * 0.9f),
                radius = 28f * cloud.scale,
                center = Offset(cloudX + 55f * cloud.scale, cloudY),
            )
            drawCircle(
                color = cloudColor.copy(alpha = cloudAlpha * 0.7f),
                radius = 20f * cloud.scale,
                center = Offset(cloudX + 75f * cloud.scale, cloudY + 5f),
            )
        }

//трава
        val grassColors = listOf(
            // 1Ц: Рассвет
            listOf(
                Color.Transparent,
                Color(0xFFE8F5E9).copy(alpha = 0.35f),  // очень светлый зелено‑голубой
                Color(0xFFC5E1A5)                             // светло‑оливковый
            ),
            // 2Ц: День
            listOf(
                Color.Transparent,
                Color(0xFF228B22).copy(alpha = 0.3f),  // зелёный
                Color(0xFF2E7D32)
            ),
            // 3Ц: Закат
            listOf(
                Color.Transparent,
                Color(0xFFFFD180).copy(alpha = 0.4f),  // золотистый персиковый
                Color(0xFFF59D57)                           // терракотово‑оранжевый
            ),
            // 4Ц: Ночь
            listOf(
                Color.Transparent,
                Color(0xFF1B4332).copy(alpha = 0.3f),  // тёмно‑зелёный
                Color(0xFF0D2818)
            )
        )
        val currentGradient =
            when {
                // 1. Рассвет → День (dayProgress < 0.3)
                isDay && dayProgress < 0.3 -> {
                    val progress = dayProgress / 0.3f  // нормализуем к [0..1]
                    blendColorLists(grassColors[0], grassColors[1], progress)
                }
                // 2. День → Закат (dayProgress > 0.9)
                isDay && dayProgress > 0.9 -> {
                    val progress = (dayProgress - 0.9f) / 0.1f  // [0..1]
                    blendColorLists(grassColors[1], grassColors[2], progress)
                }
                // 3. Закат → Ночь (!isDay && dayProgress < 0.3)
                !isDay && dayProgress < 0.3 -> {
                    val progress = dayProgress / 0.3f
                    blendColorLists(grassColors[2], grassColors[3], progress)
                }
                // 4. Ночь → Рассвет (!isDay && dayProgress > 0.8)
                !isDay && dayProgress > 0.8 -> {
                    val progress = (dayProgress - 0.8f) / 0.2f
                    blendColorLists(grassColors[3], grassColors[0], progress)
                }
                // По умолчанию: сохраняем последний цвет фазы
                isDay -> grassColors[1]  // День
                else -> grassColors[3]     // Ночь
            }

        drawRect(
            brush = Brush.verticalGradient(
                colors = currentGradient,
                startY = height * 0.85f,
                endY = height,
            ),
            topLeft = Offset(0f, height * 0.85f),
            size = Size(width, height * 0.15f),
        )
    }


}

private fun blendColorLists(
    start: List<Color>,
    end: List<Color>,
    progress: Float
): List<Color> {
    return List(3) { i ->
        lerp(start[i], end[i], progress)
    }
}
