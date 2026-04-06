package com.example.bopit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import kotlin.random.Random

class MainActivity : AppCompatActivity()
{
    final val N = 3

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val roundsInput = findViewById<EditText>(R.id.roundsInput)
        val seedInput = findViewById<EditText>(R.id.seedInput)
        val startButton = findViewById<Button>(R.id.startButton)

        startButton.setOnClickListener {

            val rounds = roundsInput.text.toString().toIntOrNull() ?: 3
            val seed = seedInput.text.toString().toIntOrNull() ?: Random.nextInt(0, Int.MAX_VALUE - 1)

            val gameModes = mutableListOf<Boolean>()

            for (i in 0 until N)
            {
                val idName = "mode$i"

                val resId = resources.getIdentifier(idName, "id", packageName)
                val checkBox = findViewById<CheckBox>(resId)

                gameModes.add(checkBox.isChecked)
            }

            val data = GameSettings(rounds, seed, gameModes)

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("GAME_SETTINGS", data)

            startActivity(intent)
        }
    }
}