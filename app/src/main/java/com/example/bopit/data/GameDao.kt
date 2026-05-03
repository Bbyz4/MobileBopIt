package com.example.bopit.data

import androidx.room.*

@Dao
interface GameDao
{
    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Insert
    suspend fun insertGameModes(modes: List<GameModeEntity>)

    @Transaction
    suspend fun insertGameWithModes(
        game: GameEntity,
        modes: List<Int>
    ){
        val gameID = insertGame(game).toInt()

        val modeEntitties = modes.map {
            GameModeEntity(gameID, it)
        }

        insertGameModes(modeEntitties)
    }

    @Query("SELECT * FROM games ORDER BY gameTime DESC")
    suspend fun getAllGames(): List<GameEntity>

    @Query("SELECT gameModeID FROM game_modes WHERE gameID = :gameId")
    suspend fun getModesForGame(gameId: Int): List<Int>
}