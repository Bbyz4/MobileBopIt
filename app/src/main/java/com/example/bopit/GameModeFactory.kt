package com.example.bopit

import android.content.Context
import android.widget.FrameLayout

object GameModeFactory
{
    fun Create(modeID: Int, context: Context, container: FrameLayout): GameMode
    {
        return when (modeID)
        {
            0 -> throw IllegalArgumentException("Unknown mode")
            else -> throw IllegalArgumentException("Unknown mode")
        }
    }
}