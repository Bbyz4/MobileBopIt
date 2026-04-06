package com.example.bopit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            var totalScore = 0

            for (step in pattern) {
                val gameMode = GameModeFactory.Create(step.taskID, this@GameActivity, container)

                val score = gameMode.run()

                totalScore += score

                delay((step.delay * 1000).toLong())
            }

        }

        //do something at the end
    }
}