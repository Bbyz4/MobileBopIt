package com.example.bopit

import android.content.Context
import android.widget.FrameLayout
import com.example.bopit.gamemodes.*

data class GameModeDescriptor (
    val gameModeInstance : GameMode,
    val gameModeTitle : String,
    val gameModeDesc : String
)

object GameModeFactory
{
    var N = 4

    fun GetGamemodeNumber() : Int
    {
        return N
    }

    fun Create(modeID: Int, context: Context, container: FrameLayout): GameModeDescriptor
    {
        return when (modeID)
        {
            0 -> GameModeDescriptor(MultitapGameMode(context, container), "TAP IT!", "Click on 10 targets as fast as you can (0)")
            1 -> GameModeDescriptor(TiltGameMode(context, container), "TILT IT!", "Tilt your device to the given angle (1)")
            2 -> GameModeDescriptor(VoiceGameMode(context, container), "SCREAM IT!", "Speak at given volume (2)")
            3 -> GameModeDescriptor(ShakeGameMode(context, container), "SHAKE IT!", "Shake your device for 5 seconds (3)")
            else -> throw IllegalArgumentException("Unknown mode")
        }
    }
}