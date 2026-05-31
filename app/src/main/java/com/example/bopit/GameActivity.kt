package com.example.bopit

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.bopit.bluetooth.BluetoothConnectionManager
import com.example.bopit.bluetooth.BluetoothMessage
import com.example.bopit.data.AppDatabase
import com.example.bopit.data.GameEntity
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity()
{
    override fun onBackPressed()
    {

    }

    private var isMultiplayer = false
    private var isHost = false
    private var myScore = 0
    private var opponentScore: Int? = null
    private var gameFinished = false
    private var finalDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        isMultiplayer = intent.getBooleanExtra("MULTIPLAYER", false)
        isHost = intent.getBooleanExtra("MULTIPLAYER_HOST", false)
        val opponentName = intent.getStringExtra("OPPONENT_NAME") ?: "Opponent"

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bopit-db"
        ).build()

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

        if(isMultiplayer)
        {
            BluetoothConnectionManager.onMessageReceivedCallback = { message ->
                if(message.messageType == "MyScore")
                {
                    opponentScore = message.playerScore
                    if(gameFinished)
                    {
                        runOnUiThread {
                            showCombinedScoreDialog(opponentName)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch{

            scoreText.text = "Score: $totalScore"

            delay(1000)

            for ((index, step) in pattern.withIndex()) {

                Log.d("GAME", "Starting round $index (mode=${step.taskID})")

                val gameModeDescriptor = GameModeFactory.Create(step.taskID, this@GameActivity, container)

                delay((step.delay * 1000).toLong())

                taskTitle.text = gameModeDescriptor.gameModeTitle
                taskDescription.text = gameModeDescriptor.gameModeDesc

                val mediaPlayer = MediaPlayer.create(this@GameActivity, gameModeDescriptor.soundResId)
                mediaPlayer.start()

                mediaPlayer.setOnCompletionListener{
                    it.release()
                }

                val score = gameModeDescriptor.gameModeInstance.run()

                taskTitle.text = "NO CHALLENGE"
                taskDescription.text = "Be prepared..."

                totalScore += score
                scoreText.text = "Score: $totalScore"

                Log.d("GAME", "Round $index score: $score")
                Log.d("GAME", "Total score so far: $totalScore")
            }

            Log.d("GAME", "Game finished, final score $totalScore")
            myScore = totalScore
            gameFinished = true

            db.gameDao().insertGameWithModes(
                GameEntity(
                    score = totalScore,
                    roundNumber = data.rounds,
                    seed = data.seed,
                    gameTime = System.currentTimeMillis(),
                    opponentName = if (isMultiplayer) opponentName else null,
                    opponentScore = opponentScore
                ),
                modes = data.gameModes.mapIndexedNotNull{ index, enabled ->
                    if(enabled) index else null
                }
            )

            if(isMultiplayer)
            {
                BluetoothConnectionManager.sendMessage(
                    BluetoothMessage(
                        messageType = "MyScore",
                        playerScore = totalScore
                    )
                )

                if(opponentScore != null)
                {
                    showCombinedScoreDialog(opponentName)
                }
                else
                {
                    finalDialog = AlertDialog.Builder(this@GameActivity)
                        .setTitle("Game finished!")
                        .setMessage("Your score: $totalScore \n Waiting for opponent...")
                        .setCancelable(false)
                        .show()
                }
            }
            else
            {
                ShowFinishedGameDialog(totalScore)
            }
        }
    }

    private fun showCombinedScoreDialog(opponentName: String)
    {
        finalDialog?.dismiss()
        val oppScore = opponentScore ?: 0
        val message = "Your score: $myScore\n$opponentName score: $oppScore"
        AlertDialog.Builder(this)
            .setTitle("Game finished!")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Back to Menu") {_, _ -> finishAndGoToMenu() }
            .show()
    }

    private fun finishAndGoToMenu()
    {
        BluetoothConnectionManager.disconnect()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
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

    override fun onDestroy()
    {
        super.onDestroy()

        if(isMultiplayer)
        {
            BluetoothConnectionManager.onMessageReceivedCallback = null
        }
    }
}