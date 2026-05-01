package com.example.bopit.gamemodes

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import android.widget.FrameLayout
import com.example.bopit.R
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
        val tolerance = 5.0

        val angleText = android.widget.TextView(context).apply {
            textSize = 24f
            text = "Tilt to: ${targetAngle.toInt()}°"
        }

        container.addView(angleText)

        val createdViews = mutableListOf<android.view.View>()

        container.post {

            val wheelSize = 600
            val targetSize = 80

            val wheel = android.widget.ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(wheelSize, wheelSize)
            }

            container.addView(wheel)

            wheel.x = (container.width - wheelSize) / 2f
            wheel.y = (container.height - wheelSize) / 2f

            val target = android.widget.ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(targetSize, targetSize)
                setImageResource(R.drawable.target)
            }

            val radius = wheelSize / 2f - targetSize

            val angleRad = Math.toRadians(targetAngle)

            val centerX = wheel.x + wheelSize / 2f
            val centerY = wheel.y + wheelSize / 2f

            val targetX = centerX + radius * kotlin.math.cos(angleRad) - targetSize / 2
            val targetY = centerY + radius * kotlin.math.sin(angleRad) - targetSize / 2

            target.x = targetX.toFloat()
            target.y = targetY.toFloat()

            container.addView(target)

            // keep references for cleanup
            createdViews.add(wheel)
            createdViews.add(target)
        }

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
                    createdViews.forEach { container.removeView(it) }
                    createdViews.clear()

                    val elapsedMs = SystemClock.elapsedRealtime() - startTime
                    val elapsedSec = elapsedMs / 1000.0

                    val score = (100 - 10 * elapsedSec).toInt().coerceAtLeast(0)

                    if(cont.isActive)
                    {
                        cont.resume(score)
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
            createdViews.forEach { container.removeView(it) }
            createdViews.clear()
        }

    }
}