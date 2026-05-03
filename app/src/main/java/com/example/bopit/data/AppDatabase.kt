package com.example.bopit.data

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [GameEntity::class, GameModeEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun gameDao(): GameDao
}
