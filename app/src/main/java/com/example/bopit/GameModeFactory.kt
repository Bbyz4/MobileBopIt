package com.example.bopit

import android.content.Context
import android.widget.FrameLayout
import com.example.bopit.gamemodes.GameMode
import com.example.bopit.gamemodes.MultitapGameMode

data class GameModeDescriptor (
    val gameModeInstance : GameMode,
    val gameModeTitle : String,
    val gameModeDesc : String
)

object GameModeFactory
{
    fun Create(modeID: Int, context: Context, container: FrameLayout): GameModeDescriptor
    {
        return when (modeID)
        {
            0 -> GameModeDescriptor(MultitapGameMode(context, container), "TAP IT!", "Click on 10 targets as fast as you can (0)")
            1 -> GameModeDescriptor(MultitapGameMode(context, container), "TAP IT!", "Same thing, different ID (1)")
            2 -> GameModeDescriptor(MultitapGameMode(context, container), "TAP IT!", "Same thing, different ID (2)")
            else -> throw IllegalArgumentException("Unknown mode")
        }
    }
}