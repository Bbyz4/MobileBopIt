package com.example.bopit.gamemodes

import android.content.Context
import android.os.SystemClock
import android.widget.Button
import android.widget.FrameLayout
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.random.Random
import kotlin.coroutines.resume

class MultitapGameMode(
    context: Context,
    container: FrameLayout
) : GameMode(context, container)
{
    override suspend fun run(): Int = suspendCancellableCoroutine {cont ->
        val startTime = SystemClock.elapsedRealtime()

        var clicks = 0
        var maxClicks = 10

        val activeTargets = mutableListOf<Button>()

        fun spawnTarget()
        {
            val button = Button(context)
            button.text = "Tap"

            val size = 200

            val maxX = container.width - size
            val maxY = container.height - size

            val x = if (maxX > 0) Random.nextInt(maxX) else 0
            val y = if (maxY > 0) Random.nextInt(maxY) else 0

            val params = FrameLayout.LayoutParams(size, size)
            button.layoutParams = params

            button.x = x.toFloat()
            button.y = y.toFloat()

            container.addView(button)
            activeTargets.add(button)

            button.setOnClickListener {
                container.removeView(button)
                activeTargets.remove(button)

                clicks++

                if(clicks >= maxClicks)
                {
                    val elapsedMs = SystemClock.elapsedRealtime() - startTime
                    val elapsedSec = elapsedMs / 1000.0

                    val score = (100 - 10 * elapsedSec).toInt().coerceAtLeast(0)

                    activeTargets.forEach { container.removeView(it) }
                    activeTargets.clear()

                    if (cont.isActive)
                    {
                        cont.resume(score)
                    }
                }
                else
                {
                    spawnTarget()
                }
            }
        }

        repeat(3)
        {
            spawnTarget()
        }
    }
}