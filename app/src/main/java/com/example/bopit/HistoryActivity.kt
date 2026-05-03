package com.example.bopit

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.bopit.data.AppDatabase
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish() // go back to MainActivity
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bopit-db"
        ).build()

        lifecycleScope.launch {
            val games = db.gameDao().getAllGames()

            val container = findViewById<LinearLayout>(R.id.historyContainer)

            games.forEach { game ->

                val modes = db.gameDao().getModesForGame(game.gameID)

                val text = "Score: ${game.score} | Modes: $modes"

                val tv = TextView(this@HistoryActivity).apply {
                    this.text = text
                }

                container.addView(tv)
            }
        }
    }
}