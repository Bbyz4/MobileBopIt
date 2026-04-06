package com.example.bopit

import android.content.Context
import android.widget.FrameLayout

abstract class GameMode(
    protected val context: Context,
    protected val container: FrameLayout
)
{
    abstract suspend fun run(): Int
}