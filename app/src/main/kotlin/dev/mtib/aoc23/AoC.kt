package dev.mtib.aoc23

import dev.mtib.aoc23.day.DayModule
import dev.mtib.aoc23.utils.AbstractDay
import dev.mtib.aoc23.utils.DayRunner
import dev.mtib.aoc23.utils.DaySolver
import dev.mtib.aoc23.utils.Knowledge
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.error.InstanceCreationException
import org.koin.ksp.generated.module
import kotlin.math.pow
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

fun main(args: Array<String>) {
    startKoin {
        modules(DayModule().module)
    }

    when (val arg = args.getOrNull(0)) {
        null -> runLastDay()
        "all" -> runAllDays()
        else -> runDay(arg.toInt())
    }
}

private fun runLastDay() {
    val day = GlobalContext.get().getAll<AbstractDay>().maxByOrNull { it.dayNumber } ?: throw ClassNotFoundException()
    runDay(day.dayNumber)
}

private fun runAllDays() {
    val days = GlobalContext.get().getAll<AbstractDay>().sortedBy { it.dayNumber }
    for (day in days) {
        runDay(day.dayNumber)
    }
}

private fun runDay(dayNumber: Int) {
    println("Running day $dayNumber\n")

    val day = try {
        GlobalContext.get().getAll<AbstractDay>().find { it.dayNumber == dayNumber } ?: throw ClassNotFoundException()
    } catch (e: InstanceCreationException) {
        val cause = e.cause
        println("\u001b[31mFailed to instantiation runner for day $dayNumber: ${cause?.toString() ?: e.toString()}\u001b[0m")
        exitProcess(1)
    } catch (e: ClassNotFoundException) {
        println("\u001b[31mNo solution found for day $dayNumber\u001b[0m")
        exitProcess(2)
    }

    for (part in 1..2) {
        runPart(day, part)
        Knowledge.KnowledgeFile.createDay(day.yearNumber, day.dayNumber, part.toLong())
        timePart(day, part, day.bufferedInput ?: emptyArray())
        println("")
    }
}

private fun runPart(day: DayRunner, part: Int) {
    println("\u001b[1mPart $part:\u001b[0m")
    // time runtime
    when (part) {
        1 -> day.runPart1()
        2 -> day.runPart2()
        else -> throw IllegalArgumentException("Part $part is not a valid part.")
    }
}

val MIN_TIME_SPENT = 500.milliseconds
const val MIN_RUNS = 20
const val MAX_RUNS = 1000

private fun timePart(day: DaySolver, part: Int, input: Array<String>) {
    try {
        buildList<Duration> {
            while (size < MIN_RUNS || reduce { acc, it -> acc + it } < MIN_TIME_SPENT) {
                add(measureTime {
                    when (part) {
                        1 -> day.solvePart1(input)
                        2 -> day.solvePart2(input)
                        else -> throw IllegalArgumentException("Part $part is not a valid part.")
                    }
                })
                if (size >= MAX_RUNS) break
            }
        }.let { durations ->
            val average = durations.sumOf { it.inWholeMicroseconds }.toDouble() / durations.size
            val standardDeviation =
                kotlin.math.sqrt(durations.sumOf { (it.inWholeMicroseconds - average).pow(2) } / durations.size)
            if (average < 1000) {
                println(
                    "\u001b[36mRuntime: ${(average).toPrecision(1)}µs, σ: ${
                        (standardDeviation).toPrecision(
                            2
                        )
                    }µs (${durations.size} runs)\u001b[0m"
                )
            } else {
                println(
                    "\u001b[36mRuntime: ${(average / 1000.0).toPrecision(1)}ms, σ: ${
                        (standardDeviation / 1000.0).toPrecision(
                            1
                        )
                    }ms (${durations.size} runs)\u001b[0m"
                )
            }
        }
    } catch (e: Exception) {
        println("Error while timing part $part: ${e.javaClass.simpleName} ${e.message}")
    }
}

private fun Double.toPrecision(precision: Int): Double = (this * 10.0.pow(precision)).toInt() / 10.0.pow(precision)