package com.example.bopit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val gameID: Int = 0,

    val score: Int,
    val seed: Int,
    val gameTime: Long,

    val opponentName: String?,
    val opponentScore: Int?
)