package com.example.bopit

import android.os.Bundle
import android.view.Gravity
import android.view.View
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

    private fun createGameItemView(
        game: com.example.bopit.data.GameEntity,
        enabledModes: List<Int>
    ) : View
    {
        val context = this

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(context, R.drawable.panel_border)
            setPadding(
                dpToPx(context, 12),
                dpToPx(context, 12),
                dpToPx(context, 12),
                dpToPx(context, 12)
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(context, 8)
            layoutParams = params
        }

        val leftColumn = LinearLayout(context).apply{
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        leftColumn.addView(
            TextView(context).apply{
                text = "Rounds: ${game.roundNumber} | Seed: ${game.seed}"
                setTextColor(ContextCompat.getColor(context, R.color.text_light))
                textSize = 14f
            }
        )

        val typeText = if (game.opponentName == null)
        {
            "SOLO"
        }
        else
        {
            "MULTI vs ${game.opponentName}, Score: ${game.opponentScore ?: "?"}"
        }
        leftColumn.addView(
            TextView(context).apply{
                text = typeText
                setTextColor(ContextCompat.getColor(context, R.color.text_light))
                textSize = 14f
                setPadding(0, dpToPx(context, 4), 0,0)
            }
        )

        val modeNames = enabledModes.map {modeName(it)}
        val modesText = if (modeNames.isEmpty()) "No modes" else modeNames.joinToString(", ")
        leftColumn.addView(
            TextView(context).apply{
                text = "Modes: $modesText"
                setTextColor(ContextCompat.getColor(context, R.color.text_light))
                textSize = 14f
                setPadding(0, dpToPx(context, 4), 0,0)
            }
        )

        val scorePerRound = if ( game.roundNumber != 0) game.score.toDouble() / game.roundNumber.toDouble() else 0

        val scoreView = TextView(context).apply {
            text = String.format("%.1f", scorePerRound)
            textSize = 28f
            setTextColor(ContextCompat.getColor(context, R.color.text_light))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            setPadding(dpToPx(context, 16), 0, 0, 0)
        }

        card.addView(leftColumn)
        card.addView(scoreView)

        return card
    }

    private fun modeName(modeId: Int): String = when (modeId) {
        0 -> "Tap"
        1 -> "Turn"
        2 -> "Scream"
        3 -> "Shake"
        else -> "Unknown mode"
    }

    private fun dpToPx(context: android.content.Context, dp: Int): Int
    {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}