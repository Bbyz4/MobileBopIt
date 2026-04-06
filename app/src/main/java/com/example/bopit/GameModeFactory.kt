package com.example.bopit

import android.content.Context
import android.widget.FrameLayout
import com.example.bopit.gamemodes.GameMode
import com.example.bopit.gamemodes.MultitapGameMode

object GameModeFactory
{
    fun Create(modeID: Int, context: Context, container: FrameLayout): GameMode
    {
        return when (modeID)
        {
            0 -> MultitapGameMode(context, container)
            1 -> MultitapGameMode(context, container)
            2 -> MultitapGameMode(context, container)
            else -> throw IllegalArgumentException("Unknown mode")
        }
    }
}