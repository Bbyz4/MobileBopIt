package com.example.bopit.gamemodes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.math.abs

class ShakeGameMode(
    context: Context,
    container: FrameLayout
) : GameMode(context, container)
{
    override suspend fun run(): Int = suspendCancellableCoroutine { cont ->

        val startTime = SystemClock.elapsedRealtime()

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val textView = TextView(context).apply {
            textSize = 22f
            text = "SHAKE!"
        }

        container.addView(textView)

        var shakeCount = 0

        var lastX = 0f
        var lastDirection = 0

        val threshold = 12f

        val listener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent) {

                val x = event.values[0]

                val diff = x - lastX

                val currentDirection = when {
                    diff > 0 -> 1
                    diff < 0 -> -1
                    else -> 0
                }

                val force = abs(diff)

                if(currentDirection != 0 && currentDirection != lastDirection && force > threshold)
                {
                    shakeCount++
                    textView.text = "Shakes: $shakeCount"
                }

                lastDirection = currentDirection
                lastX = x
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        val scope = CoroutineScope(Dispatchers.Main)

        val job = scope.launch{
            delay(5000)

            sensorManager.unregisterListener(listener)
            container.removeView(textView)

            val elapsedMs = SystemClock.elapsedRealtime() - startTime

            val score = (shakeCount * 10).coerceAtMost(100)

            if(cont.isActive)
            {
                cont.resume(score)
            }
        }

        cont.invokeOnCancellation {
            job.cancel()
            sensorManager.unregisterListener(listener)
            container.removeView(textView)
        }
    }
}