package ru.gorinih.daynightshow.screens.firework

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.Boolean
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class FireworkType() {
    SIMPLE,
    ARROW,
    ARROW_SPEED,
    ELLIPSE,
    COLORIZE_ELLIPSE,
}

data class FireworkModel(
    var positionX: Float, // стартовая позиция по Х
    var positionY: Float, // стартовая позиция по Y
    var moveX: Float,
    var moveY: Float, // скорость лета
    var targetY: Float, // высока куда прилетит перед взрывом
    var isFlightUp: Boolean = true, // направление полета вверх или вниз
    val size: Float, // толщина заряда
    var color: Color, // цвет заряда
    var isExploded: Boolean = false,
    val tailParticles: MutableList<FireworkParticleModel> = mutableListOf(),
    val explodeParticles: MutableList<FireworkParticleModel> = mutableListOf(),
    val aftershockParticles: MutableList<FireworkParticleModel> = mutableListOf(),
    var typeFire: FireworkType = FireworkType.SIMPLE
)

data class FireworkParticleModel(
    var positionX: Float,
    var positionY: Float,
    var moveX: Float,
    var moveY: Float,
    var currentLife: Float,
    var maxLife: Float,
    var color: Color,
    var size: Float,
)

private val colorsOfFirework = listOf(
    Color(0xFFD3928F),
    Color(0xFFFF7E79),
    Color(0xFFFFD479),
    Color(0xFFECCD90),
    Color(0xFFD4FB79),
    Color(0xFF49FA79),
    Color(0xFF49FCD6),
    Color(0xFF4AD6FF),
    Color(0xFF7A81FF),
    Color.White,
    Color(0xFFD883FF),
)

object FireworkSimple {
    private val fireworks = mutableListOf<FireworkModel>()
    private val rnd = Random

    fun update(width: Float, height: Float, deltaTime: Float, isRunning: Boolean) {
        if (rnd.nextFloat() < 0.05f && fireworks.count() < 10 && isRunning)
        //  if (fireworks.isEmpty())
            addFirework(width, height)

        val iterator = fireworks.iterator()

        while (iterator.hasNext()) {
            val firework = iterator.next()

            /**
             * если еще не взорвался то лети вверх/вниз с хвостом
             */
            when {
                !firework.isExploded -> {
                    firework.positionY += firework.moveY * deltaTime
                    firework.moveY += 50f * deltaTime // с затуханием полета
                    if (firework.isFlightUp && firework.moveY > 0) {
                        firework.targetY = firework.positionY + 300f * rnd.nextFloat()
                        firework.isFlightUp = false
                    }

                    if (rnd.nextFloat() < 0.8f) {
                        firework.tailParticles.add(
                            FireworkParticleModel(
                                firework.positionX + rnd.nextFloat() * 4f - 2f,
                                positionY = firework.positionY,
                                moveX = rnd.nextFloat() * 20f - 10f,
                                moveY = rnd.nextFloat() * 50f + 20f,
                                currentLife = rnd.nextFloat().coerceIn(0.1f, 0.6f), // длина хвоста
                                maxLife = 0.5f,
                                color = firework.color.copy(alpha = 0.8f),
                                size = rnd.nextFloat() * 3f + 1f,
                            )
                        )
                    }
                }

                // и так тут только те где firework.isExploded && т.е. взорвались
                firework.typeFire == FireworkType.ARROW || firework.typeFire == FireworkType.ARROW_SPEED -> {
                    firework.explodeParticles.forEach { ex ->
                        if (rnd.nextFloat() < 0.8f) {
                            firework.tailParticles.add(
                                FireworkParticleModel(
                                    positionX = ex.positionX + rnd.nextFloat() * 4f - 2f,
                                    positionY = ex.positionY,
                                    moveX = rnd.nextFloat() * 20f - 10f,
                                    moveY = rnd.nextFloat() * 50f + 20f,
                                    currentLife = 1f,// rnd.nextFloat().coerceIn(0.1f,0.6f), // длина хвоста
                                    maxLife = 1f,
                                    color = ex.color.copy(alpha = 0.8f),
                                    size = rnd.nextFloat() * 3f + 1f,
                                )
                            )
                        }
                    }
                }
            }

            /**
             * хвост ракеты и взрыв
             */
            updateParticles(firework.tailParticles, deltaTime, gravity = 90f)
            updateParticles(firework.explodeParticles, deltaTime, gravity = 10f)
            updateParticles(firework.aftershockParticles, deltaTime, gravity = 90f)

            /**
             * окончание полета инициация взрыва
             */
            when {
                firework.isFlightUp && firework.positionY < firework.targetY && !firework.isExploded -> explode(
                    firework
                )

                !firework.isFlightUp && firework.positionY > firework.targetY && !firework.isExploded -> explode(
                    firework
                )
            }

            if (firework.isExploded &&
                firework.tailParticles.isEmpty() &&
                firework.explodeParticles.isEmpty() &&
                firework.aftershockParticles.isEmpty()
            ) {
                iterator.remove()
            }
        }
    }

    private fun explode(firework: FireworkModel) {
        firework.isExploded = true
        when (firework.typeFire) {
            FireworkType.SIMPLE -> simpleFirework(firework)
            FireworkType.ARROW -> arrowFirework(firework)
            FireworkType.ARROW_SPEED -> arrowAnySpeedFirework(firework)
            FireworkType.ELLIPSE -> ellipseFirework(firework)
            FireworkType.COLORIZE_ELLIPSE -> colorizeEllipseFirework(firework)
        }
    }

    private fun arrowFirework(firework: FireworkModel) {
        val particleCount = 30
        val life = rnd.nextFloat() * 1.7f + 1f
        val speed = rnd.nextFloat() * 200f + 50f
        for (i in 0 until particleCount) {
            val angle = i * 2 * PI.toFloat() / particleCount
            val alpha = 0.5f * rnd.nextFloat()
            firework.explodeParticles.add(
                FireworkParticleModel(
                    positionX = firework.positionX,
                    positionY = firework.positionY,
                    moveX = cos(angle) * speed * 1.4f,
                    moveY = sin(angle) * speed * 0.7f,// 1.4f,
                    currentLife = life,
                    maxLife = life,
                    color = firework.color.copy(alpha = alpha),
                    size = rnd.nextFloat() * 4f + 2f,
                )
            )
        }
    }

    private fun arrowAnySpeedFirework(firework: FireworkModel) {
        val particleCount = 30
        val life = rnd.nextFloat() * 1.5f + 1f
        for (i in 0 until particleCount) {
            val angle = i * 2 * PI.toFloat() / particleCount
            val alpha = 0.5f * rnd.nextFloat()
            val speed = rnd.nextFloat() * 200f + 50f
            firework.explodeParticles.add(
                FireworkParticleModel(
                    positionX = firework.positionX,
                    positionY = firework.positionY,
                    moveX = cos(angle) * speed,
                    moveY = sin(angle) * speed * 0.6f,
                    currentLife = life,
                    maxLife = life,
                    color = firework.color.copy(alpha = alpha),
                    size = rnd.nextFloat() * 4f + 2f,
                )
            )
        }
    }

    private fun ellipseFirework(firework: FireworkModel) {
        val particleCount = 20
        val speed = rnd.nextFloat() * 200f + 50f
        val life = rnd.nextFloat() * 1.5f + 1f
        var angle = 1f
        firework.explodeParticles.clear()
        for (i in 0 until particleCount) {
            angle *= rnd.nextFloat().coerceIn(10f, 59f) * PI.toFloat()
            firework.explodeParticles.add(
                FireworkParticleModel(
                    positionX = firework.positionX,
                    positionY = firework.positionY,
                    moveX = sin(angle) * speed * 2,
                    moveY = cos(angle) * speed,
                    currentLife = life,
                    maxLife = life,
                    color = firework.color,
                    size = rnd.nextFloat() * 4f + 2f,
                ),
            )
        }
    }

    private fun colorizeEllipseFirework(firework: FireworkModel) {
        val particleCount = 20
        val speed = rnd.nextFloat() * 200f + 50f
        val life = rnd.nextFloat() * 1.5f + 1f
        var angle = 1f
        firework.explodeParticles.clear()
        for (i in 0 until particleCount) {
            angle *= rnd.nextFloat().coerceIn(10f, 59f) * PI.toFloat()
            val color = colorsOfFirework[rnd.nextInt(colorsOfFirework.count() - 1)]
            firework.explodeParticles.add(
                FireworkParticleModel(
                    positionX = firework.positionX,
                    positionY = firework.positionY,
                    moveX = sin(angle) * speed * 2,
                    moveY = cos(angle) * speed,
                    currentLife = life,
                    maxLife = life,
                    color = color,
                    size = rnd.nextFloat() * 4f + 2f,
                ),
            )
        }
    }

    private fun simpleFirework(firework: FireworkModel) {
        val particleCount = rnd.nextInt(80) + 120
        for (i in 0 until particleCount) {
            val angle = rnd.nextFloat() * 2f * PI.toFloat()
            val speed = rnd.nextFloat() * 200f + 50f
            val life = rnd.nextFloat() * 1.5f + 1f

            firework.explodeParticles.add(
                FireworkParticleModel(
                    positionX = firework.positionX,
                    positionY = firework.positionY,
                    moveX = cos(angle) * speed,
                    moveY = sin(angle) * speed,
                    currentLife = life,
                    maxLife = life,
                    color = firework.color,
                    size = rnd.nextFloat() * 4f + 2f,
                ),
            )
        }
    }

    private fun updateParticles(
        particles: MutableList<FireworkParticleModel>,
        deltaTime: Float,
        gravity: Float,
    ) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.positionX += particle.moveX * deltaTime
            particle.positionY += particle.moveY * deltaTime
            particle.moveY += gravity * deltaTime
            particle.moveX *= 0.99f
            particle.currentLife -= deltaTime

            if (particle.currentLife <= 0) {
                iterator.remove()
            }
        }
    }

    fun addFirework(width: Float, height: Float) {
        val color = colorsOfFirework[rnd.nextInt(colorsOfFirework.count() - 1)]
        fireworks.add(
            FireworkModel(
                positionX = rnd.nextFloat() * width * 0.8f + width * 0.1f,
                positionY = height,
                moveX = 0f,
                moveY = -(rnd.nextFloat() * 100f + 400f),
                targetY = rnd.nextFloat() * height * 0.4f + height * 0.1f,
                color = color,
                size = (rnd.nextFloat() * 10f).coerceIn(2f, 8f),
                typeFire = //FireworkType.ARROW_SPEED
                    FireworkType.entries[rnd.nextInt(5)]
            )
        )
    }

    fun draw(drawScope: DrawScope) {
        fireworks.forEach { firework ->
            if (!firework.isExploded) {
                drawScope.drawCircle(
                    radius = firework.size,
                    color = firework.color,
                    center = Offset(firework.positionX, firework.positionY),
                    blendMode = BlendMode.Plus,
                )
            }

            firework.tailParticles.forEach { particle ->
                val alpha = (particle.currentLife / particle.maxLife).coerceIn(0f, 1f)
                drawScope.drawCircle(
                    color = particle.color.copy(alpha = alpha * 0.7f),
                    radius = particle.size,
                    center = Offset(particle.positionX, particle.positionY),
                    blendMode = BlendMode.Plus,
                )
            }

            firework.explodeParticles.forEach { particle ->
                val alpha = (particle.currentLife / particle.maxLife).coerceIn(0f, 1f)
                val size = particle.size * (0.3f + alpha * 0.7f)

                drawScope.drawCircle(
                    color = particle.color.copy(alpha = alpha * 0.3f),
                    radius = size * 2f,
                    center = Offset(particle.positionX, particle.positionY),
                    blendMode = BlendMode.Plus,
                )

                drawScope.drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = size,
                    center = Offset(particle.positionX, particle.positionY),
                    blendMode = BlendMode.Plus,
                )
            }
        }
    }
}

@Composable
fun FireworkSimpleScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF1A1A3A),
                        Color(0xFF0A0A1A),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        FireworkRunning(true)
    }
}

/**
 * функция запуска фейерверков
 */
@Composable
fun FireworkRunning(isRunning: Boolean) {
    val fireworksObject = remember { FireworkSimple }
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var shouldRedraw by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        suspend fun animateFrame() {
            withFrameNanos { frameTimeNanos ->
                val deltaFrameTime = if (lastFrameTimeNanos == 0L) {
                    0.016f
                } else {
                    ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
                }
                lastFrameTimeNanos = frameTimeNanos
                fireworksObject.update(
                    canvasSize.width,
                    canvasSize.height,
                    deltaFrameTime,
                    isRunning
                )
                shouldRedraw = true
            }
            animateFrame()
        }
        animateFrame() // первый запуск функции
    }

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        canvasSize = size
        if (shouldRedraw) {
            fireworksObject.draw(this)
            shouldRedraw = false
        }
    }


}
