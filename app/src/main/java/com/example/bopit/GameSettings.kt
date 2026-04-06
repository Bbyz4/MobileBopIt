package com.example.bopit

import java.io.Serializable

data class GameSettings(
    val rounds: Int,
    val seed: Int,
    val gameModes: List<Boolean>
) : Serializable