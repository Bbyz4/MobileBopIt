package com.example.bopit

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.bopit.data.AppDatabase
import com.google.android.material.internal.ViewUtils.dpToPx
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

                val text = "Normalized score: ${game.score.toDouble() / game.roundNumber.toDouble()} Raw score: ${game.score} | Modes: $modes"

                val tv = TextView(this@HistoryActivity).apply {
                    this.text = text

                    setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat)

                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.text_light
                        )
                    )

                    setPadding(
                        12,
                        12,
                        12,
                        12
                    )

                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.panel_border
                    )

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.bottomMargin = 8
                    layoutParams = params
                }

                container.addView(tv)
            }
        }
    }
}