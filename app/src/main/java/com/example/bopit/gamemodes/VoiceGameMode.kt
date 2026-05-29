package com.example.bopit.gamemodes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.provider.MediaStore.Audio
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.bopit.R
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.random.Random

class VoiceGameMode(
    context: Context,
    container: FrameLayout
) : GameMode(context, container)
{
    var currentPointer: ImageView? = null
    var uiReady = false

    private fun SetImagePosition(image: ImageView, decibels: Double)
    {
        val baseX = container.width / 2f
        val baseY = container.height / 2f

        val scale = 5f

        val y = baseY - (scale * decibels)

        image.x = baseX - (image.width / 2f)
        image.y = y.toFloat()
    }

    override suspend fun run(): Int = suspendCancellableCoroutine { cont ->

        val createdViews = mutableListOf<android.view.View>()

        val permission = android.Manifest.permission.RECORD_AUDIO

        val targetSize = 80

        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
        {
            cont.resume(0)

            return@suspendCancellableCoroutine
        }

        val startTime = SystemClock.elapsedRealtime()

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val targetVolume = Random.nextDouble(40.0, 80.0)

        val target = android.widget.ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(targetSize, targetSize)
            setImageResource(R.drawable.target)
        }

        SetImagePosition(target, targetVolume)

        container.addView(target)

        currentPointer = android.widget.ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(targetSize, targetSize)
            setImageResource(R.drawable.target)
        }

        SetImagePosition(currentPointer!!, 0.0)

        container.addView(currentPointer)

        createdViews.add(target)
        createdViews.add(currentPointer!!)

        val volumes = mutableListOf<Double>()

        fun amplitudeToDb(amplitude: Double): Double {
            return 20 * log10(amplitude.coerceAtLeast(1.0))
        }

        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch{

            val buffer = ShortArray(bufferSize)

            audioRecord.startRecording()

            repeat(100) { i ->

                if(!isActive)
                {
                    return@launch
                }

                val read = audioRecord.read(buffer, 0, buffer.size)

                var sum = 0.0

                for (j in 0 until read)
                {
                    sum += buffer[j] * buffer[j]
                }

                val rms = sqrt(sum / read.coerceAtLeast(1))

                val db = amplitudeToDb(rms)

                volumes.add(db)

                Log.d("VOICE", "Current dB value: ${db.toInt()}")

                SetImagePosition(currentPointer!!, db)

                delay(100)
            }

            audioRecord.stop()
            audioRecord.release()

            val avg = volumes.average()
            val diff = kotlin.math.abs(avg - targetVolume) //to fix

            val score = (100 - diff * 2).toInt().coerceIn(0, 100)

            withContext(Dispatchers.Main)
            {
                createdViews.forEach { container.removeView(it) }
                createdViews.clear()
            }

            val elapsedMs = SystemClock.elapsedRealtime() - startTime

            if(cont.isActive)
            {
                cont.resume(score)
            }
        }

        cont.invokeOnCancellation {
            job.cancel()
            audioRecord.release()
            createdViews.forEach { container.removeView(it) }
            createdViews.clear()
        }
    }
}