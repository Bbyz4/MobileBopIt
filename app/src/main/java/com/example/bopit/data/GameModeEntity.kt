package com.example.bopit.data

import androidx.room.Entity
import androidx.room.ForeignKey


@Entity(
    tableName = "game_modes",
    primaryKeys = ["gameID", "gameModeID"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["gameID"],
            childColumns = ["gameID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GameModeEntity(
    val gameID: Int,
    val gameModeID: Int
)