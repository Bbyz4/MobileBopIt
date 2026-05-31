package com.example.bopit.bluetooth

import com.example.bopit.GameSettings
import java.io.Serializable

data class BluetoothMessage(
    val messageType: String,
    val playerName: String? = null,
    val settings: GameSettings? = null,
    val playerScore: Int? = null
) : Serializable