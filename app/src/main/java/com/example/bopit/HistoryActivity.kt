package com.example.bopit

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            finish() // go back to MainActivity
        }
    }
}