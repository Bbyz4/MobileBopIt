package com.example.bopit

import kotlin.random.Random

data class TaskEntry(
    var taskID: Int,
    var delay: Float
)


class TaskPatternGenerator(private val settings : GameSettings)
{
    private val random: Random = Random(settings.seed)

    fun GetRandomPattern() : List<TaskEntry>
    {
        var enabledModes = settings.gameModes.mapIndexedNotNull {index, enabled -> if(enabled) index else null }

        if(enabledModes.isEmpty())
        {
            return emptyList()
        }

        val pattern = mutableListOf<TaskEntry>()

        repeat(settings.rounds)
        {
            val gameMode = enabledModes[random.nextInt(enabledModes.size)]

            val delay = random.nextDouble(2.0,10.0).toFloat()

            pattern.add(TaskEntry(gameMode,delay))
        }

        return pattern
    }
}