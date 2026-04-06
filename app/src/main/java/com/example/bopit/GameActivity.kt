package com.example.bopit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val container = findViewById<FrameLayout>(R.id.gameContainer)

        val data = intent.getSerializableExtra("GAME_SETTINGS") as GameSettings

        val generator = TaskPatternGenerator(data)
        val pattern = generator.GetRandomPattern()

        lifecycleScope.launch{

            Log.d("GAME", "Game starting in 5 seconds")
            delay(5000)

            var totalScore = 0

            for ((index, step) in pattern.withIndex()) {

                Log.d("GAME", "Starting round $index (mode=${step.taskID}")

                val gameMode = GameModeFactory.Create(step.taskID, this@GameActivity, container)

                val score = gameMode.run()

                totalScore += score

                Log.d("GAME", "Round $index score: $score")
                Log.d("GAME", "Total score so far: $totalScore")

                delay((step.delay * 1000).toLong())
            }

            Log.d("GAME", "Game finished, final score $totalScore")
        }

        //do something at the end
    }
}