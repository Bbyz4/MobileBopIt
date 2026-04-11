package com.example.bopit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity()
{
    override fun onBackPressed()
    {

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val container = findViewById<FrameLayout>(R.id.gameContainer)

        val data = intent.getSerializableExtra("GAME_SETTINGS") as GameSettings

        val generator = TaskPatternGenerator(data)
        val pattern = generator.GetRandomPattern()


        val scoreText = findViewById<TextView>(R.id.scoreText)
        val taskTitle = findViewById<TextView>(R.id.taskTitle)
        val taskDescription = findViewById<TextView>(R.id.taskDescription)

        taskTitle.text = "NO CHALLENGE"
        taskDescription.text = "Be prepared..."

        var totalScore = 0

        lifecycleScope.launch{

            scoreText.text = "Score: $totalScore"

            delay(1000)

            for ((index, step) in pattern.withIndex()) {

                Log.d("GAME", "Starting round $index (mode=${step.taskID})")

                val gameModeDescriptor = GameModeFactory.Create(step.taskID, this@GameActivity, container)

                delay((step.delay * 1000).toLong())

                taskTitle.text = gameModeDescriptor.gameModeTitle
                taskDescription.text = gameModeDescriptor.gameModeDesc

                val score = gameModeDescriptor.gameModeInstance.run()

                taskTitle.text = "NO CHALLENGE"
                taskDescription.text = "Be prepared..."

                totalScore += score
                scoreText.text = "Score: $totalScore"

                Log.d("GAME", "Round $index score: $score")
                Log.d("GAME", "Total score so far: $totalScore")
            }

            Log.d("GAME", "Game finished, final score $totalScore")

            ShowFinishedGameDialog(totalScore)
        }
    }

    private fun ShowFinishedGameDialog(finalScore: Int)
    {
        AlertDialog.Builder(this)
            .setTitle("Game finished!")
            .setMessage("Your score: $finalScore")
            .setCancelable(false)
            .setPositiveButton("Back to Menu") {_,_ ->
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .show()

    }
}