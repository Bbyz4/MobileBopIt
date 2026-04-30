package com.example.bopit.gamemodes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import android.widget.FrameLayout
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.random.Random

class TiltGameMode(
    context: Context,
    container: FrameLayout
) : GameMode(context, container)
{
    override suspend fun run(): Int = suspendCancellableCoroutine { cont ->

        val startTime = SystemClock.elapsedRealtime()

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val targetAngle = Random.nextDouble(0.0, 360.0)
        val tolerance = 20.0

        val angleText = android.widget.TextView(context).apply {
            textSize = 24f
            text = "Tilt to: ${targetAngle.toInt()}°"
        }

        container.addView(angleText)

        fun angleDifference(a: Double, b: Double): Double
        {
            val diff = abs(a-b)
            return minOf(diff, 360 - diff)
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener
        {
            override fun onSensorChanged(event: SensorEvent)
            {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)

                var angle = Math.toDegrees(orientation[0].toDouble())

                if(angle < 0)
                {
                    angle += 360
                }

                Log.d("TILT", "Current angle: $angle")

                val diff = angleDifference(angle, targetAngle)

                if(diff <= tolerance)
                {
                    sensorManager.unregisterListener(this)
                    container.removeView(angleText)

                    val elapsedMs = SystemClock.elapsedRealtime() - startTime
                    val elapsedSec = elapsedMs / 1000.0

                    val score = (100 - 10 * elapsedSec).toInt().coerceAtLeast(0)

                    if(cont.isActive)
                    {
                        cont.resume(100)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
            {

            }
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        cont.invokeOnCancellation {
            sensorManager.unregisterListener(listener)
            container.removeView(angleText)
        }

    }
}